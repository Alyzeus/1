@echo off
cd /d "%~dp0"
if not exist "target\classes\com\travel\Main.class" (
    echo Please run compile.bat first!
    pause
    exit /b 1
)
set CLASSPATH=target\classes;lib\tomcat-embed-core-9.0.85.jar;lib\tomcat-embed-jasper-9.0.85.jar;lib\tomcat-embed-el-9.0.85.jar;lib\tomcat-annotations-api-9.0.85.jar;lib\mysql-connector-j-8.0.33.jar;lib\ecj-3.26.0.jar
if "%PORT%"=="" set PORT=8090
echo Starting... http://localhost:%PORT%/
"C:\Program Files\Java\jdk1.8.0_102\bin\java.exe" -Dport=%PORT% -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -cp "%CLASSPATH%" com.travel.Main
pause
