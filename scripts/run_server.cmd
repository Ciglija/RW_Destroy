@echo off
call ..\venv\Scripts\activate
set PYTHONPATH=..\backend
waitress-serve --host=192.168.0.30 --port=5000 flask_box_management_app:app
pause