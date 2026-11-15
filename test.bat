@echo off
chcp 65001 >nul
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt test\usst\mud\SmokeTest.java
del sources.txt
java -Dstdout.encoding=UTF-8 -Dfile.encoding=UTF-8 -cp out usst.mud.SmokeTest
pause