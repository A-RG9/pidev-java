@echo off
set JFX_PATH=javafx-sdk-17.0.2\lib
set CP=.;bcrypt-0.10.2.jar;bytes-1.5.0.jar;mysql-connector-j-8.3.0.jar;javax.mail-1.6.2.jar;activation-1.1.1.jar;src\main\resources

echo ========================================
echo    WellCare Desktop Launcher
echo    (Compiling and Launching...)
echo ========================================

:: Ensure out directory exists
if not exist out mkdir out

:: Sync resources (FXML, CSS, Images) to out directory
echo [1/3] Syncing resources...
xcopy /S /Y /I src\main\resources\* out\ > nul

:: Compile code
echo [2/3] Compiling source code...
javac --module-path "%JFX_PATH%" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "%CP%" -d out ^
src/main/java/com/wellcare/javafx/MainApplication.java ^
src/main/java/com/wellcare/javafx/controller/admin/*.java ^
src/main/java/com/wellcare/javafx/controller/auth/*.java ^
src/main/java/com/wellcare/javafx/controller/dashboard/*.java ^
src/main/java/com/wellcare/javafx/dao/*.java ^
src/main/java/com/wellcare/javafx/model/*.java ^
src/main/java/com/wellcare/javafx/service/*.java ^
src/main/java/com/wellcare/javafx/util/*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Compilation failed! Please check the errors above.
    pause
    exit /b %ERRORLEVEL%
)

:: Run Application
echo [3/3] Launching JavaFX GUI (Landing Page)...
echo.
java --module-path "%JFX_PATH%" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "out;%CP%" com.wellcare.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Application exited with error code %ERRORLEVEL%
    pause
)

echo.
echo Application closed.
pause
