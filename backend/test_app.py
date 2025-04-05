# import pytest
# from backend.flask_box_management_app import app, engine, USER_FILE_PATH, BOX_DB_FILE_PATH
# import pandas as pd
# from werkzeug.security import generate_password_hash
#
# @pytest.fixture
# def client():
#     with app.test_client() as client:
#         yield client
#
# @pytest.fixture(scope="module", autouse=True)
# def setup_users():
#     df = pd.DataFrame([{
#         'employee': 'Test User',
#         'username': 'testuser',
#         'password': generate_password_hash('password123'),
#         'status': 'Admin',
#         'admin_code_info': 'Ima sifra adminpass'
#     }])
#     df.to_sql('users', con=engine, if_exists='replace', index=False)
#
# @pytest.fixture(scope="module")
# def token(client):
#     response = client.post('/login', json={
#         'username': 'testuser',
#         'password': 'password123'
#     })
#     return response.get_json()['access_token']
#
# def test_login_success(client):
#     response = client.post('/login', json={
#         'username': 'testuser',
#         'password': 'password123'
#     })
#     assert response.status_code == 200
#     assert 'access_token' in response.get_json()
#
# def test_login_fail(client):
#     response = client.post('/login', json={
#         'username': 'wronguser',
#         'password': 'wrongpass'
#     })
#     assert response.status_code == 401
#
#
# def test_import_users(client):
#     response = client.post('/import-users')
#     print("IMPORT USERS RESPONSE:", response.get_json())
#     assert response.status_code == 200
#     assert "Users imported successfully" in response.get_json()['message']
#
#
# def test_load_database(client):
#     response = client.post('/load-database')
#     print("LOAD DATABASE RESPONSE:", response.get_json())
#     assert response.status_code == 200
#     assert "Box database loaded successfully" in response.get_json()['message']
#
#
# def test_scan_box_valid(client, token):
#     box_code = 'BOX123'
#     df = pd.DataFrame([{
#         'client': 'Client1',
#         'box': box_code,
#         'batch': 'Batch1',
#         'status': 0,
#         'scanned_by': None,
#         'scan_time': None
#     }])
#     df.to_sql('boxes', con=engine, if_exists='append', index=False)
#
#     response = client.post('/scan-box', json={'box_code': box_code},
#                            headers={'Authorization': f'Bearer {token}'})
#     assert response.status_code == 200
#     data = response.get_json()
#     assert data['status'] == 'valid'
#
# def test_scan_box_invalid(client, token):
#     response = client.post('/scan-box', json={'box_code': 'INVALIDBOX'},
#                            headers={'Authorization': f'Bearer {token}'})
#     assert response.status_code == 404
#
# def test_admin_auth_success(client, token):
#     response = client.post('/admin-auth', json={
#         'admin_username': 'testuser',
#         'admin_password': 'adminpass'
#     }, headers={'Authorization': f'Bearer {token}'})
#     assert response.status_code == 200
#     assert 'Admin privileges granted' in response.get_json()['message']
#
# def test_admin_auth_fail(client, token):
#     response = client.post('/admin-auth', json={
#         'admin_username': 'testuser',
#         'admin_password': 'wrongpass'
#     }, headers={'Authorization': f'Bearer {token}'})
#     assert response.status_code == 403
#
# def test_generate_report(client):
#     response = client.get('/generate-report')
#     assert response.status_code == 200
#     assert 'report_path' in response.get_json()
#
#
