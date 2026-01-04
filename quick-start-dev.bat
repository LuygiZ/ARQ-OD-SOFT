@echo off
echo ====================================
echo  LMS - Quick Start Development
echo ====================================
echo.

echo [1/4] Checking Docker...
docker ps >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not running!
    echo Please start Docker Desktop and try again.
    pause
    exit /b 1
)
echo ✓ Docker is running

echo.
echo [2/4] Building all services...
echo This may take 5-10 minutes on first run...
docker-compose -f docker-compose-dev.yml build

if errorlevel 1 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)
echo ✓ Build complete

echo.
echo [3/4] Starting all services...
docker-compose -f docker-compose-dev.yml up -d

if errorlevel 1 (
    echo ERROR: Failed to start services!
    pause
    exit /b 1
)

echo ✓ Services started

echo.
echo [4/4] Waiting for services to initialize (60 seconds)...
timeout /t 60 /nobreak >nul

echo.
echo ====================================
echo  Services are ready!
echo ====================================
echo.
echo Access points:
echo   - Genre Service:     http://localhost:8080/swagger-ui/index.html
echo   - Author Service:    http://localhost:8082/swagger-ui/index.html
echo   - Book Command:      http://localhost:8083/swagger-ui/index.html
echo   - Book Query:        http://localhost:8085/swagger-ui/index.html
echo   - Lending Service:   http://localhost:8086/swagger-ui/index.html
echo   - Reader Service:    http://localhost:8087/swagger-ui/index.html
echo   - Saga Orchestrator: http://localhost:8084/swagger-ui/index.html
echo.
echo Infrastructure:
echo   - PostgreSQL:  localhost:5432 (postgres/password)
echo   - MongoDB:     localhost:27017 (admin/admin123)
echo   - Redis:       localhost:6379
echo   - RabbitMQ UI: http://localhost:15672 (guest/guest)
echo.
echo Check status: docker-compose -f docker-compose-dev.yml ps
echo View logs:    docker-compose -f docker-compose-dev.yml logs -f
echo Stop all:     docker-compose -f docker-compose-dev.yml down
echo.
pause
