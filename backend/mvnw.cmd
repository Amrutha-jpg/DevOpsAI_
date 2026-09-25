@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------

@echo off
setlocal

set MAVEN_CMD_LINE_ARGS=%*
if "%JAVA_HOME%" == "" goto error_no_javahome

set MAVEN_HOME=%~dp0\.mvn\wrapper
if not exist "%MAVEN_HOME%\maven-wrapper.jar" (
    echo Downloading Maven Wrapper...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar', '%MAVEN_HOME%\maven-wrapper.jar')"
)

"%JAVA_HOME%\bin\java.exe" "-Dmaven.multiModuleProjectDirectory=%~dp0." -classpath "%MAVEN_HOME%\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %MAVEN_CMD_LINE_ARGS%
goto end

:error_no_javahome
echo ERROR: JAVA_HOME is not set in your environment.
exit /B 1

:end
endlocal
