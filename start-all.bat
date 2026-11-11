@echo off
echo ===================================================
echo Starting ACE TeamUp Development Servers
echo ===================================================

echo [1/3] Starting Spring Boot Backend (Port 8080)...
start "ACE TeamUp - Spring Boot Backend" cmd /k "cd /d D:\KIT HACKATHON\backend && mvn spring-boot:run"

echo [2/3] Starting Python FastAPI AI Service (Port 8000)...
start "ACE TeamUp - Python AI Service" cmd /k "cd /d D:\KIT HACKATHON\ai_service && python main.py"

echo [3/3] Starting React Frontend (Port 3000)...
start "ACE TeamUp - React Frontend" cmd /k "cd /d D:\KIT HACKATHON\frontend && npm.cmd run dev"

echo.
echo All services are launching in separate windows!
echo - Frontend: http://localhost:3000
echo - Backend API: http://localhost:8080/api
echo - AI Service: http://localhost:8000/api/ai/health
echo ===================================================
pause

