@echo off
setlocal
cd /d "%~dp0"
echo Can chay server truoc. Kiem thu se them du lieu TEST vao database demo.
javac --release 17 -encoding UTF-8 -d target/test-classes src/main/java/vn/edu/clinic/common/Request.java src/main/java/vn/edu/clinic/common/Response.java src/test/java/vn/edu/clinic/SocketSmokeTest.java
if errorlevel 1 exit /b 1
java -cp "target/test-classes;dist/clinic-desktop-v2.jar;lib/*" vn.edu.clinic.SocketSmokeTest
if errorlevel 1 exit /b 1
