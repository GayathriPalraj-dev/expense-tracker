@echo off
setlocal
for %%I in ("%~dp0.") do set "MAVEN_PROJECTBASEDIR=%%~fI"
if not "%JAVA_HOME%"=="" (
  set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA_EXE=java.exe"
)
if not exist "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
  echo Maven Wrapper bootstrap JAR is missing. See README.md for setup. 1>&2
  exit /b 1
)
"%JAVA_EXE%" -classpath "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
exit /b %ERRORLEVEL%
