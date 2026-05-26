@echo off
setlocal

if "%MYSQL_HOST%"=="" set MYSQL_HOST=127.0.0.1
if "%MYSQL_PORT%"=="" set MYSQL_PORT=3306
if "%MYSQL_USER%"=="" set MYSQL_USER=root
if "%MYSQL_DB%"=="" set MYSQL_DB=mofadanqing

if "%MYSQL_PASSWORD%"=="" (
  set /p MYSQL_PASSWORD=MySQL password:
)

mysql --force --default-character-set=utf8mb4 -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% < "%~dp0backend\src\main\resources\sql\sample_data.sql"

endlocal
pause
