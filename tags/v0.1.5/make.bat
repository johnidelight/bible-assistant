@echo off
echo.
echo ********************** Preparing Content **********************
echo.
@echo on

cd content
call publish.bat
cd ..

@echo off
echo.
echo ********************** Building Product **********************
echo.
@echo on

cd build
call init.bat
call clean.bat
call release.bat
cd ..

@echo off
echo.
echo ********************** Distributing Product *******************
echo.
@echo on

rd /s /q dist
md dist
copy platform\android\src\bin\BibleAssistant-release.apk dist\

@echo off
echo.
echo ********************** FINISHED !!! ****************************
echo.
@echo on

pause
