@echo off
call ..\venv\Scripts\activate.bat
python ..\backend\load_database.py
pause
