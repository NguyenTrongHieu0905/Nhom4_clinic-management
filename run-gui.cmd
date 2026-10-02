@echo off
setlocal
cd /d "%~dp0"
java -cp "dist/clinic-desktop-v2.jar;lib/*" vn.edu.clinic.client.ClinicDesktop %*
if errorlevel 1 pause
