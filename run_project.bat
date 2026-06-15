@echo off
echo ===================================================
echo             STARTING LIFELINK APP
echo ===================================================
echo.
echo Make sure MySQL server is running on port 3306.
echo.
cd /d "%~dp0"
call .\maven\apache-maven-3.9.6\bin\mvn.cmd javafx:run
pause
