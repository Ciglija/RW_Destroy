from flask import Flask, request, jsonify
from flask_jwt_extended import (
    JWTManager, create_access_token,
    jwt_required, get_jwt_identity
)
from werkzeug.security import generate_password_hash, check_password_hash
from sqlalchemy import create_engine, text
import pandas as pd
import os
from datetime import timedelta

app = Flask(__name__)

app.config["JWT_SECRET_KEY"] = os.getenv("JWT_SECRET_KEY")
app.config["JWT_ACCESS_TOKEN_EXPIRES"] = timedelta(hours=1)
jwt = JWTManager(app)

os.makedirs('backend/database', exist_ok=True)
engine = create_engine('sqlite:///backend/database/RWdestroydb.db')

USER_FILE_PATH = 'backend/excel_files/users.xlsx'
BOX_DB_FILE_PATH = 'backend/excel_files/boxes.xlsx'
REPORT_FILE_PATH = 'backend/reports/scanned_boxes_report.xlsx'
os.makedirs('reports', exist_ok=True)


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
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/load-database', methods=['POST'])
def load_database():
    try:
        df = pd.read_excel(BOX_DB_FILE_PATH)
        df.rename(columns={
            'Klijent': 'client',
            'Kutija': 'box',
            'Tura': 'batch'
        }, inplace=True)

        df['status'] = 0
        df['scanned_by'] = None
        df['scan_time'] = None

        df.to_sql('boxes', con=engine, if_exists='replace', index=False)
        return jsonify({"message": "Box database loaded successfully"}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/scan-box', methods=['POST'])
@jwt_required()
def scan_box():
    current_user = get_jwt_identity()
    box_code = request.json.get('box_code')

    if not box_code:
        return jsonify({"error": "No box code provided"}), 400

    try:
        box = pd.read_sql(
            "SELECT * FROM boxes WHERE box = ?",
            con=engine,
            params=(box_code,)
        )

        if box.empty:
            print("Nema kutije")
            return jsonify({"status": "invalid", "message": "Box invalid"}), 404

        scan_time = pd.Timestamp.now().strftime('%Y-%m-%d %H:%M:%S')

        with engine.begin() as connection:
            connection.execute(
                text("""
                    UPDATE boxes 
                    SET status = 1, 
                        scanned_by = :scanned_by, 
                        scan_time = :scan_time 
                    WHERE box = :box_code
                """),
                {
                    'scanned_by': current_user,
                    'scan_time': scan_time,
                    'box_code': box_code
                }
            )

        return jsonify({
            "status": "valid",
            "message": "Box scanned successfully",
            "scanned_by": current_user,
            "scan_time": scan_time
        }), 200

    except Exception as e:
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

    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/generate-report', methods=['GET'])
def generate_report():
    try:
        boxes_query = text("SELECT * FROM boxes")
        all_boxes = pd.read_sql(boxes_query, con=engine)
        print(all_boxes)
        all_boxes['status'] = all_boxes['status'].apply(
            lambda x: 'True' if x else 'False'
        )

        all_boxes[['client', 'box', 'batch', 'scanned_by', 'scan_time', 'status']] \
            .to_excel(REPORT_FILE_PATH, index=False)

        return jsonify({
            "message": f"Report generated at {REPORT_FILE_PATH}",
            "report_path": REPORT_FILE_PATH
        }), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/')
def home():
    return "Box Management API is running!"


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000, debug=True)
