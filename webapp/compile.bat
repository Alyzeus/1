@echo off
cd /d "%~dp0"
set CLASSPATH=lib\tomcat-embed-core-9.0.85.jar;lib\tomcat-embed-jasper-9.0.85.jar;lib\tomcat-embed-el-9.0.85.jar;lib\tomcat-annotations-api-9.0.85.jar;lib\postgresql-42.7.4.jar;lib\ecj-3.26.0.jar;lib\jbcrypt-0.4.jar;lib\HikariCP-4.0.3.jar;lib\slf4j-api-1.7.36.jar;lib\slf4j-simple-1.7.36.jar
if not exist "target\classes" mkdir "target\classes"
javac -encoding UTF-8 -cp "%CLASSPATH%" -d "target\classes" -sourcepath "src\main\java" src\main\java\com\travel\*.java src\main\java\com\travel\util\*.java src\main\java\com\travel\web\*.java
if %errorlevel% equ 0 (echo [OK] Compile success) else (echo [FAIL] Compile error)
pause
