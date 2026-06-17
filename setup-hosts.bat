@echo off
echo ========================================
echo   CodeCollab hosts 配置工具
echo ========================================
echo.

:: 获取WSL IP
for /f "tokens=*" %%i in ('wsl hostname -I') do set WSL_IP=%%i
for /f "tokens=1" %%a in ("%WSL_IP%") do set WSL_IP=%%a

echo WSL IP: %WSL_IP%
echo.

:: 检查hosts文件中是否已存在
findstr /C:"codecollab.local" C:\Windows\System32\drivers\etc\hosts >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] codecollab.local 已存在于hosts文件中
    echo.
    echo 当前配置:
    findstr /C:"codecollab.local" C:\Windows\System32\drivers\etc\hosts
    echo.
    set /p UPDATE="是否更新IP? (Y/N): "
    if /i not "%UPDATE%"=="Y" goto :end
    
    :: 删除旧配置
    powershell -Command "(Get-Content C:\Windows\System32\drivers\etc\hosts) | Where-Object { $_ -notmatch 'codecollab.local' } | Set-Content C:\Windows\System32\drivers\etc\hosts"
)

:: 添加新配置
echo %WSL_IP% codecollab.local >> C:\Windows\System32\drivers\etc\hosts
echo.
echo [OK] 已添加配置: %WSL_IP% codecollab.local
echo.

:end
echo ========================================
echo   配置完成!
echo   请访问: http://codecollab.local
echo ========================================
pause
