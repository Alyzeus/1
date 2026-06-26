@echo off
cd /d "%~dp0"
set CLASSPATH=lib\tomcat-embed-core-9.0.85.jar;lib\tomcat-embed-jasper-9.0.85.jar;lib\tomcat-embed-el-9.0.85.jar;lib\tomcat-annotations-api-9.0.85.jar;lib\mysql-connector-j-8.0.33.jar;lib\ecj-3.26.0.jar
if not exist "target\classes" mkdir "target\classes"
"C:\Program Files\Java\jdk1.8.0_102\bin\javac.exe" -encoding UTF-8 -cp "%CLASSPATH%" -d "target\classes" -sourcepath "src\main\java" src\main\java\com\travel\*.java src\main\java\com\travel\util\*.java src\main\java\com\travel\web\*.java
if %errorlevel% equ 0 (echo [OK]) else (echo [FAIL])
pause
