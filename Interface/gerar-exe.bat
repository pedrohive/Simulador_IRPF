@echo off
rem Gera o aplicativo "Simulador IRPF.exe" (com Java embutido) em dist\Simulador IRPF\
cd /d "%~dp0"

call mvnw.cmd -q package -DskipTests || goto :erro

if exist dist rmdir /s /q dist
"%JAVA_HOME%\bin\jpackage.exe" --type app-image --name "Simulador IRPF" ^
  --input target --main-jar simulador-irpf.jar --main-class br.com.simulador.irpf.Launcher ^
  --add-modules java.base,java.desktop,java.logging,java.xml,java.prefs,jdk.unsupported ^
  --java-options "-Dfile.encoding=UTF-8" --dest dist || goto :erro

echo.
echo Pronto: dist\Simulador IRPF\Simulador IRPF.exe
pause
exit /b 0

:erro
echo Falha ao gerar o aplicativo.
pause
exit /b 1
