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
from utils import (
    setup_box_stats_and_triggers,
)


load_dotenv()

app = Flask(__name__)

app.config["JWT_SECRET_KEY"] = os.getenv("JWT_SECRET_KEY")
app.config["JWT_ACCESS_TOKEN_EXPIRES"] = timedelta(hours=1)
jwt = JWTManager(app)

app.config['MAIL_SERVER'] = 'smtp.gmail.com' #smtp.office365.com
app.config['MAIL_PORT'] = 587
app.config['MAIL_USE_TLS'] = True
app.config['MAIL_USERNAME'] = os.getenv("EMAIL")
app.config['MAIL_PASSWORD'] = os.getenv("PASSWORD")
app.config['MAIL_DEFAULT_SENDER'] = ('RW_Uništavanje', os.getenv("EMAIL"))

mail = Mail(app)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

DB_DIR = os.path.join(BASE_DIR, 'database')
os.makedirs(DB_DIR, exist_ok=True)
engine = create_engine(f'sqlite:///{os.path.join(DB_DIR, "RWdestroydb.db")}')

USER_FILE_PATH = os.path.join(BASE_DIR, 'excel_files', 'users.xlsx')
BOX_DB_FILE_PATH = os.path.join(BASE_DIR, 'excel_files', 'boxes.xlsx')
REPORT_DIR = os.path.join(BASE_DIR, 'reports')
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
            'Login name': 'username',
            'Lozinka': 'password',
            'Vrsta korisnika': 'status',
        }, inplace=True)

        df['password'] = df['password'].apply(generate_password_hash)
        df.to_sql('users', con=engine, if_exists='replace', index=False)
        return jsonify({"message": "Users imported successfully with hashed passwords"}), 200
    except Exception:
        return jsonify({"error": "Internal server error"}), 500


@app.route('/load-database', methods=['POST'])
def load_database():
    global counter
    try:
        df = pd.read_excel(BOX_DB_FILE_PATH, dtype={"Kutija": str})
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
        setup_box_stats_and_triggers()
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

        with engine.begin() as connection:
            connection.execute(text(query), params)

            result = connection.execute(text("SELECT unscanned_count FROM box_stats WHERE id = 1"))
            unscanned = result.scalar()
        if not box.empty:
            return jsonify({
                "message": "Box scanned successfully",
                "already_scanned": False,
                "unscanned": unscanned,
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

        if not check_password_hash(user.iloc[0]['password'], password):
            return jsonify({"error": "Invalid credentials"}), 401

        if user.iloc[0]['status'] != 'Admin':
            return jsonify({"error": "Admin access required"}), 403

        return jsonify({"message": "Admin privileges granted"}), 200

    except Exception:
        return jsonify({"error": "Internal server error"}), 500

@app.route('/get-missing-count', methods=['GET'])
def get_missing_count():
    try:
        with engine.connect() as connection:
            result = connection.execute(text("SELECT unscanned_count FROM box_stats WHERE id = 1"))
            return jsonify({"unscanned": result.scalar()}), 200
    except Exception:
        return jsonify({"error": "Internal server error"}), 500

@app.route('/get-missing-boxes', methods=['GET'])
def get_missing_boxes():
    try:
        with engine.connect() as connection:
            result = connection.execute(text("SELECT * FROM boxes WHERE status = FALSE"))
            rows = result.fetchall()
            missing_boxes = [dict(row._mapping) for row in rows]
            return jsonify({"missing_boxes": missing_boxes}), 200
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
        all_boxes["present"] = all_boxes["present"].apply(lambda x: "Da" if x  else "Ne")
        all_boxes['box'] = all_boxes['box'].astype(str)
        all_boxes = all_boxes.rename(columns={
            'client': 'Klijent',
            'box': 'Kutija',
            'batch': 'Tura',
            'scanned_by': 'Skenirao',
            'scan_time': 'Vreme skeniranja',
            'present': 'Nalazila se u bazi'
        })
        df_client = pd.read_excel(BOX_DB_FILE_PATH, nrows=1)
        client_name = df_client.at[0, "Klijent"].replace(" ", "_")
        timestamp = pd.Timestamp.now().strftime("%Y-%m-%d-%H-%M-%S")
        report_filename = f"{timestamp}_{client_name}.xlsx"
        report_path = os.path.join(REPORT_DIR, report_filename)
        with pd.ExcelWriter(report_path, engine='xlsxwriter') as writer:
            selected_columns = ['Klijent', 'Kutija', 'Tura', 'Skenirao', 'Vreme skeniranja', 'Nalazila se u bazi']
            all_boxes[selected_columns].to_excel(writer, index=False, sheet_name='Izvestaj')
            workbook = writer.book
            worksheet = writer.sheets['Izvestaj']
            text_format = workbook.add_format({'num_format': '@'})
            worksheet.set_column('B:B', 20, text_format)
        msg = Message('Izveštaj sa uništavanja', recipients=[os.getenv("EMAIL")])
        msg.body = 'Završeno skeniranje, Izveštaj se nalazi u prilogu.'
        with app.open_resource(report_path) as fp:
            msg.attach(report_filename, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", fp.read())
        mail.send(msg)
        return jsonify({
            "message": f"Report generated",
        }), 200
    except Exception:
        return jsonify({"error": "Internal server error"}), 500


@app.route('/')
def home():
    return "Box Management API is running!"


if __name__ == '__main__':
    app.run()
