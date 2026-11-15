@echo off
chcp 65001 >nul
if not exist "out\usst\mud\Game.class" (
    echo [First run] Compiling, please wait...
    if not exist out mkdir out
    dir /s /b src\*.java > sources.txt
    javac -encoding UTF-8 -d out @sources.txt
    del sources.txt
)
echo This is a scripted demo: help / talk / walk / quiz / answer / quit.
echo.
type demo1.txt | java -Dstdout.encoding=UTF-8 -Dfile.encoding=UTF-8 -Dmud.echo=true -cp out usst.mud.Game --seed=7
echo.
echo Demo finished. Run run.bat to play by yourself.
pause