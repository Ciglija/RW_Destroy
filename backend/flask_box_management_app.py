import os
from datetime import timedelta
from flask import Flask, request, jsonify
from flask_jwt_extended import (
    JWTManager, create_access_token,
    jwt_required, get_jwt_identity
)
from flask_mail import Mail, Message
from werkzeug.security import generate_password_hash, check_password_hash
from sqlalchemy import create_engine, text
import pandas as pd

from dotenv import load_dotenv

load_dotenv()

app = Flask(__name__)

app.config["JWT_SECRET_KEY"] = os.getenv("JWT_SECRET_KEY")
app.config["JWT_ACCESS_TOKEN_EXPIRES"] = timedelta(hours=1)
jwt = JWTManager(app)

app.config['MAIL_SERVER'] = 'smtp.gmail.com'
app.config['MAIL_PORT'] = 587
app.config['MAIL_USE_TLS'] = True
app.config['MAIL_USERNAME'] = os.getenv("EMAIL")
app.config['MAIL_PASSWORD'] = os.getenv("PASSWORD")
app.config['MAIL_DEFAULT_SENDER'] = ('RW_Skeniranje', os.getenv("EMAIL"))

mail = Mail(app)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

DB_DIR = os.path.join(BASE_DIR, 'database')
os.makedirs(DB_DIR, exist_ok=True)
engine = create_engine(f'sqlite:///{os.path.join(DB_DIR, "RWdestroydb.db")}')

USER_FILE_PATH = os.path.join(BASE_DIR, 'excel_files', 'users.xlsx')
BOX_DB_FILE_PATH = os.path.join(BASE_DIR, 'excel_files', 'boxes.xlsx')
REPORT_DIR = os.path.join(BASE_DIR, 'reports')
REPORT_FILE_PATH = os.path.join(REPORT_DIR, 'scanned_boxes_report.xlsx')
os.makedirs(REPORT_DIR, exist_ok=True)

EXCEL_DIR = os.path.join(BASE_DIR, 'excel_files')
os.makedirs(EXCEL_DIR, exist_ok=True)


@app.route('/login', methods=['POST'])
def login():
    username = request.json.get('username')
    password = request.json.get('password')

    if not username or not password:
        return jsonify({"error": "Missing username or password"}), 400

    user_query = text("SELECT * FROM users WHERE username = :username")
    user = pd.read_sql(user_query, con=engine, params={'username': username})

    if user.empty or not check_password_hash(user.iloc[0]['password'], password):
        return jsonify({"error": "Invalid credentials"}), 401

    access_token = create_access_token(identity=username)
    return jsonify(access_token=access_token), 200


@app.route('/import-users', methods=['POST'])
def import_users():
    try:
        df = pd.read_excel(USER_FILE_PATH)
        df.rename(columns={
            'Zaposleni': 'employee',
            'Username': 'username',
            'Password': 'password',
            'Status': 'status',
            'Sifra admina za ispravku': 'admin_code_info'
        }, inplace=True)

        df['password'] = df['password'].apply(generate_password_hash)
        df.to_sql('users', con=engine, if_exists='replace', index=False)
        return jsonify({"message": "Users imported successfully with hashed passwords"}), 200
    except Exception:
        return jsonify({"error": "Internal server error"}), 500


@app.route('/load-database', methods=['POST'])
def load_database():
    try:
        df = pd.read_excel(BOX_DB_FILE_PATH)
        df.rename(columns={
            'Klijent': 'client',
            'Kutija': 'box',
            'Tura': 'batch'
        }, inplace=True)

        df['status'] = False
        df['present'] = True
        df['scanned_by'] = None
        df['scan_time'] = None

        df.to_sql('boxes', con=engine, if_exists='replace', index=False)
        return jsonify({"message": "Box database loaded successfully"}), 200
    except Exception:
        return jsonify({"error": "Internal server error"}), 500


@app.route('/scan-box', methods=['POST'])
@jwt_required()
def scan_box():
    try:
        current_user = get_jwt_identity()
        box_code = request.json.get('box_code')

        if not box_code:
            return jsonify({"error": "Box code is required"}), 400

        scan_time = pd.Timestamp.now().strftime('%Y-%m-%d %H:%M:%S')
        params = {
            'scanned_by': current_user,
            'scan_time': scan_time,
            'box_code': box_code
        }
        box = pd.read_sql(
            "SELECT * FROM boxes WHERE box = :box",
            con=engine,
            params={'box': box_code}
        )
        if not box.empty and box.at[0, "status"]:
            return jsonify({
                "message": "Box already scanned",
                "already_scanned": True
            }), 200
        if not box.empty and not box.at[0, "status"]:
            query = """
                UPDATE boxes 
                SET status = TRUE, 
                    scanned_by = :scanned_by, 
                    scan_time = :scan_time 
                WHERE box = :box_code
            """
        else:
            query = """
                INSERT INTO boxes (box, status, scanned_by, scan_time, present)
                VALUES (:box_code, TRUE, :scanned_by, :scan_time, FALSE)
            """

        with engine.begin() as connection:
            connection.execute(text(query), params)

        if not box.empty:
            return jsonify({
                "message": "Box scanned successfully",
                "already_scanned": False
            }), 200
        else:
            return jsonify({
                "message": "Box was not present in the database"
            }), 400

    except Exception:
        return jsonify({"error": "Internal server error"}), 500


@app.route('/admin-auth', methods=['POST'])
def admin_auth():
    try:
        data = request.get_json()

        username = data.get('admin_username')
        password = data.get('admin_password')

        if not username or not password:
            return jsonify({"error": "Both username and password are required"}), 400

        user_query = text("SELECT * FROM users WHERE username = :username")
        user = pd.read_sql(user_query, con=engine, params={'username': username})

        if user.empty:
            return jsonify({"error": "Admin not found"}), 404

        if user.iloc[0]['status'] != 'Admin' or user.iloc[0]['admin_code_info'] == 'Nema':
            return jsonify({"error": "Admin access required"}), 403


        admin_code_info = user.iloc[0]['admin_code_info']
        if not admin_code_info.startswith('Ima sifra '):
            return jsonify({"error": "Invalid admin code format"}), 400

        stored_admin_code = admin_code_info.replace('Ima sifra ', '').strip()
        if password == stored_admin_code:
            return jsonify({"message": "Admin privileges granted"}), 200
        else:
            return jsonify({"error": "Invalid admin password"}), 403

    except Exception:
        return jsonify({"error": "Internal server error"}), 500

@app.route('/get-missing-count', methods=['GET'])
def get_missing_count():
    try:
        with engine.connect() as connection:
            result = connection.execute(text("SELECT COUNT(*) FROM boxes WHERE status = FALSE"))
            count = result.scalar()
            return jsonify({"unscanned": count}), 200
    except Exception as e:
        return jsonify({"error": "Internal server error"}), 500

@app.route('/get-client-name', methods=['GET'])
def get_client_name():
    try:
        df = pd.read_excel(BOX_DB_FILE_PATH, nrows=1)
        client = df.at[0, "Klijent"]
        return jsonify({"client_name": client}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/generate-report', methods=['GET'])
def generate_report():
    try:
        boxes_query = text("SELECT * FROM boxes")
        all_boxes = pd.read_sql(boxes_query, con=engine)
        all_boxes["present"] = all_boxes["present"].apply(lambda x: "True" if x  else "False")
        all_boxes = all_boxes.rename(columns={
            'client': 'Klijent',
            'box': 'Kutija',
            'batch': 'Tura',
            'scanned_by': 'Skenirao',
            'scan_time': 'Vreme skeniranja',
            'present': 'Nalazila se u bazi'
        })
        all_boxes[['Klijent', 'Kutija', 'Tura', 'Skenirao', 'Vreme skeniranja', 'Nalazila se u bazi']] \
            .to_excel(REPORT_FILE_PATH, index=False)
        msg = Message('Report poslat',
                      recipients=['kasicilija12@email.com'])
        msg.body = 'Završeno skeniranje, Izveštaj poslat.'
        mail.send(msg)
        return jsonify({
            "message": f"Report generated at {REPORT_FILE_PATH}",
            "report_path": REPORT_FILE_PATH
        }), 200
    except Exception:
        return jsonify({"error": "Internal server error"}), 500


@app.route('/')
def home():
    return "Box Management API is running!"


if __name__ == '__main__':
    app.run(host="192.168.0.30", port=5000, debug=True)
