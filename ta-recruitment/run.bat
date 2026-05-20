@echo off
echo Starting TA Recruitment System...
echo.

REM 编译项目
mvn clean compile
if %errorlevel% neq 0 (
    echo Compilation failed. Please check for errors.
    pause
    exit /b 1
)

echo.
echo Compilation successful. Starting embedded Tomcat...
echo.

REM 启动应用
mvn exec:java
if %errorlevel% neq 0 (
    echo Failed to start the application. Please check for errors.
    pause
    exit /b 1
)