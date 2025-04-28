@echo off
call ..\venv\Scripts\activate.bat
python ..\backend\get_missing_boxes.py
pause
