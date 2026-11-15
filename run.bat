@echo off
chcp 65001 >nul
if not exist "out\usst\mud\Game.class" (
    echo [First run] Compiling, please wait...
    if not exist out mkdir out
    dir /s /b src\*.java > sources.txt
    javac -encoding UTF-8 -d out @sources.txt
    del sources.txt
    if errorlevel 1 (
        echo Compile FAILED. Please run compile.bat to see details.
        pause
        exit /b 1
    )
    echo [First run] Build OK.
)
java -Dstdout.encoding=UTF-8 -Dfile.encoding=UTF-8 -cp out usst.mud.Game %*
pause