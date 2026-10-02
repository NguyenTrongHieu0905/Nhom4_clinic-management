@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
    echo Can cai JDK 17 hoac 21 va them thu muc bin vao PATH.
    exit /b 1
)
if not exist target\classes mkdir target\classes
type nul > target\sources.txt
for /r src\main\java %%f in (*.java) do (
    set "source=%%f"
    echo "!source:\=/!">> target\sources.txt
)
javac --release 17 -encoding UTF-8 -cp "lib/*" -d target\classes @target\sources.txt
if errorlevel 1 exit /b 1
xcopy /e /i /y src\main\resources target\classes >nul
if errorlevel 1 exit /b 1
if not exist dist mkdir dist
java --module jdk.jartool/sun.tools.jar.Main --create --file dist\clinic-desktop-v2.jar -C target\classes .
if errorlevel 1 exit /b 1
echo BUILD OK
