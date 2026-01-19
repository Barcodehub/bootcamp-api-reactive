# Script de Validación - Compilación de Microservicios Bootcamp
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Validando Compilación de Microservicios" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$success = $true

# Compilar Bootcamp-API
Write-Host "[1/2] Compilando Bootcamp-API..." -ForegroundColor Yellow
Set-Location bootcamp-api
$output = .\gradlew clean build -x test 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✅ Bootcamp-API - BUILD SUCCESSFUL" -ForegroundColor Green
} else {
    Write-Host "  ❌ Bootcamp-API - BUILD FAILED" -ForegroundColor Red
    Write-Host $output
    $success = $false
}
Set-Location ..

# Compilar Capacity-API
Write-Host "[2/2] Compilando Capacity-API..." -ForegroundColor Yellow
Set-Location capacity-api
$output = .\gradlew clean build -x test 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✅ Capacity-API - BUILD SUCCESSFUL" -ForegroundColor Green
} else {
    Write-Host "  ❌ Capacity-API - BUILD FAILED" -ForegroundColor Red
    Write-Host $output
    $success = $false
}
Set-Location ..

Write-Host ""
if ($success) {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✅ TODOS LOS MICROSERVICIOS COMPILADOS OK" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Puedes iniciar los servicios con:" -ForegroundColor Cyan
    Write-Host "  1. cd bootcamp-api; .\gradlew bootRun" -ForegroundColor White
    Write-Host "  2. cd capacity-api; .\gradlew bootRun" -ForegroundColor White
} else {
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "❌ COMPILACIÓN FALLIDA" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "Revisa los errores arriba." -ForegroundColor Yellow
}
Write-Host ""

