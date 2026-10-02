@echo off
setlocal
cd /d "%~dp0"
if not exist dist\clinic-desktop-v2.jar call build.cmd
if errorlevel 1 (
    pause
    exit /b 1
)
java -cp "dist/clinic-desktop-v2.jar;lib/*" vn.edu.clinic.server.ClinicServer
pause
