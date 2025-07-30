@echo off
chcp 65001 >nul
title 백엔드 종료

echo.
echo ====================================
echo            백엔드 종료 
echo ====================================
echo.

:: Docker 서비스 상태 확인
echo 현재 서비스 상태 확인 중...
docker-compose ps 2>nul
if %errorlevel% neq 0 (
    echo  실행 중인 서비스가 없거나 Docker에 문제가 있습니다.
    echo.
    pause
    exit /b 0
)

echo.
echo 서비스 종료 중...

:: Docker 서비스 종료
docker-compose down
if %errorlevel% neq 0 (
    echo  서비스 종료에 실패했습니다.
    echo  수동으로 종료하려면 다음 명령어를 사용하세요:
    echo    docker-compose down -v  (데이터까지 삭제)
    echo    docker stop $(docker ps -q)  (모든 컨테이너 강제 종료)
    echo.
    pause
    exit /b 1
)

echo  서비스 종료 완료
echo.

echo ====================================
echo           백엔드 종료 완료
echo ====================================
echo.
pause 