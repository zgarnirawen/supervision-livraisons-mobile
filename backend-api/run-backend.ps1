#! C:\Users\ZGARNI\delivery-supervision\backend-api\run-backend.ps1
# Script PowerShell pour démarrer le backend Spring Boot avec le bon chemin vers Java

Write-Host "================================" -ForegroundColor Cyan
Write-Host "Démarrage du serveur Spring Boot" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Définir le chemin vers Java 17
$env:JAVA_HOME = "C:\java\jdk-17"
$env:PATH = "$($env:JAVA_HOME)\bin;$($env:PATH)"

# Aller au répertoire du backend
Set-Location "C:\Users\ZGARNI\delivery-supervision\backend-api"

# Compiler et packager
Write-Host "Compilation du backend..." -ForegroundColor Yellow
& mvn clean package -DskipTests -q

# Lancer le serveur
Write-Host ""
Write-Host "Lancement du serveur sur le port 8080..." -ForegroundColor Green
Write-Host ""

& "$($env:JAVA_HOME)\bin\java.exe" -jar target/livraisons-api-1.0.0.jar
