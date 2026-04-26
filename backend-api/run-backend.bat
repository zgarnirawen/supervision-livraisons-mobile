@echo off
REM Script pour démarrer le backend Spring Boot avec le bon chemin vers Java
REM Location: c:\Users\ZGARNI\delivery-supervision\backend-api\run-backend.bat

echo ================================
echo Démarrage du serveur Spring Boot
echo ================================
echo.

REM Définir le chemin vers Java 17
set JAVA_HOME=C:\java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

REM Aller au répertoire du backend
cd /d "C:\Users\ZGARNI\delivery-supervision\backend-api"

REM Compiler et packager si nécessaire
echo Compilation du backend...
call mvn clean package -DskipTests -q

REM Lancer le serveur
echo.
echo Lancement du serveur sur le port 8080...
echo.
"%JAVA_HOME%\bin\java.exe" -jar target/livraisons-api-1.0.0.jar

pause
