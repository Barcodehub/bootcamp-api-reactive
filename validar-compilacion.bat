@echo off
REM Script de Validación - Compilación de Microservicios Bootcamp
echo ========================================
echo Validando Compilacion de Microservicios
echo ========================================
echo.

REM Compilar Bootcamp-API
echo [1/2] Compilando Bootcamp-API...
cd bootcamp-api
call gradlew clean build -x test > nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Bootcamp-API - BUILD SUCCESSFUL
) else (
    echo [ERROR] Bootcamp-API - BUILD FAILED
    exit /b 1
)
cd ..

REM Compilar Capacity-API
echo [2/2] Compilando Capacity-API...
cd capacity-api
call gradlew clean build -x test > nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Capacity-API - BUILD SUCCESSFUL
) else (
    echo [ERROR] Capacity-API - BUILD FAILED
    exit /b 1
)
cd ..

echo.
echo ========================================
echo TODOS LOS MICROSERVICIOS COMPILADOS OK
echo ========================================
echo.
echo Puedes iniciar los servicios con:
echo   1. cd bootcamp-api ^&^& gradlew bootRun
echo   2. cd capacity-api ^&^& gradlew bootRun
echo.
exit /b 0

