@echo off
echo ========================================
echo    Spring Agent 项目启动脚本
echo ========================================

REM 设置 JDK 21
set JAVA_HOME=D:\soft\BellSoft\LibericaJDK-21
set PATH=%JAVA_HOME%\bin;%PATH%

REM 检查 JDK 版本
java -version

echo.
echo 正在启动 Spring Agent 项目...
echo.

REM 启动项目
mvn spring-boot:run
