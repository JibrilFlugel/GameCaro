@echo off
echo Building LWJGL3 runnable JAR...
call gradlew lwjgl3:dist

echo.
echo Running the game from JAR...
for /f %%i in ('dir /b /a:-d "lwjgl3\build\libs\*.jar"') do (
    java -jar "lwjgl3\build\libs\%%i"
    goto end
)

:end
echo.
echo Game exited.
pause
