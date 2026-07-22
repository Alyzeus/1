@echo off
cd /d "%~dp0"
if not exist "target\classes\com\travel\Main.class" (
    echo Please run compile.bat first!
    pause
    exit /b 1
)
set CLASSPATH=target\classes;lib\tomcat-embed-core-9.0.85.jar;lib\tomcat-embed-jasper-9.0.85.jar;lib\tomcat-embed-el-9.0.85.jar;lib\tomcat-annotations-api-9.0.85.jar;lib\postgresql-42.7.4.jar;lib\ecj-3.26.0.jar;lib\jbcrypt-0.4.jar;lib\HikariCP-4.0.3.jar;lib\slf4j-api-1.7.36.jar;lib\slf4j-simple-1.7.36.jar
if "%PORT%"=="" set PORT=8090
echo Starting... http://localhost:%PORT%/
java -Dport=%PORT% -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -cp "%CLASSPATH%" com.travel.Main
pause
