@echo off
chcp 65001 >nul
title 백엔드

echo.
echo ====================================
echo          백엔드 실행 시작
echo ====================================
echo.

:: Docker 설치 확인
echo Docker 상태 확인 중...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo  Docker가 설치되지 않았거나 실행되지 않습니다.
    echo  Docker Desktop을 설치하고 실행해주세요: https://www.docker.com/products/docker-desktop/
    echo.
    pause
    exit /b 1
)
echo Docker 확인 완료

:: Docker Compose 확인
echo Docker Compose 확인 중...
docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Docker Compose가 사용할 수 없습니다.
    echo Docker Desktop을 최신 버전으로 업데이트해주세요.
    echo.
    pause
    exit /b 1
)
echo Docker Compose 확인 완료

:: .env 파일 확인
echo 환경설정 파일 확인 중...
if not exist ".env" (
    echo  .env 파일이 없습니다.
    echo  env.example을 복사해서 .env 파일을 생성해주세요.
    echo 명령어: copy env.example .env
    echo.
    pause
    exit /b 1
)
echo 환경설정 파일 확인 완료

echo.
echo 백엔드 서비스 시작 중...
echo 처음 실행시 Docker 이미지 다운로드로 시간이 걸릴 수 있습니다.
echo.

:: Docker 서비스 시작
docker-compose up -d
if %errorlevel% neq 0 (
    echo  서비스 시작에 실패했습니다.
    echo  다음을 시도해보세요:
    echo    1. Docker Desktop이 실행 중인지 확인
    echo    2. 포트 8080과 5432가 다른 프로그램에서 사용 중인지 확인
    echo    3. docker-compose logs 명령어로 오류 확인
    echo.
    pause
    exit /b 1
)

echo  서비스 시작 완료
echo.
echo  서비스 상태 확인 중...
timeout /t 3 /nobreak >nul

:: 서비스 상태 표시
docker-compose ps

echo.
echo ====================================
echo  환경 준비 완료
echo ====================================
echo.
echo.

:: 15초 후 브라우저에서 Swagger UI 열기
timeout /t 15 /nobreak >nul
start http://localhost:8080/swagger-ui/index.html

echo ====================================
echo 서비스를 종료하려면 stop.bat을 실행하세요.
echo ====================================
echo.
pause 