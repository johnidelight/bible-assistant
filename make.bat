@echo off
echo.
echo ********************** Preparing content **********************
echo.
@echo on

cd content
call publish.bat
cd ..

@echo off
echo.
echo ********************** Building product **********************
echo.
@echo on

cd build
call init.bat
call release.bat
cd ..

pause
