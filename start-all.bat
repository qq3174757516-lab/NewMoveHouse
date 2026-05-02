@echo off
echo Starting all apps...
start "Admin App" cmd /k "cd /d admin-app && npm run dev"
start "Driver App" cmd /k "cd /d driver-app && npm run dev"
start "User App" cmd /k "cd /d user-app && npm run dev"
echo All dev servers have been launched in separate windows.
pause