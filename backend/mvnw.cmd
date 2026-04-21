@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET @@MVNW_LAUNCHER=%~dp0.mvn\wrapper\maven-wrapper.jar

@SET MAVEN_WRAPPER_JAR=%~dp0.mvn\wrapper\maven-wrapper.jar
@SET MAVEN_WRAPPER_PROPERTIES=%~dp0.mvn\wrapper\maven-wrapper.properties

@FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_WRAPPER_PROPERTIES%") DO (
    @IF "%%A"=="distributionUrl" SET DISTRIBUTION_URL=%%B
)

@SET MAVEN_USER_HOME=%USERPROFILE%\.m2\wrapper
@SET MAVEN_HOME=%MAVEN_USER_HOME%\dists\apache-maven-3.9.6

@IF EXIST "%MAVEN_HOME%\bin\mvn.cmd" GOTO RUN_MAVEN

@ECHO Downloading Maven 3.9.6...
@SET DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip
@SET ZIP_FILE=%MAVEN_USER_HOME%\maven.zip

@IF NOT EXIST "%MAVEN_USER_HOME%" MKDIR "%MAVEN_USER_HOME%"
@IF NOT EXIST "%MAVEN_USER_HOME%\dists" MKDIR "%MAVEN_USER_HOME%\dists"

@powershell -Command "Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%ZIP_FILE%'"
@powershell -Command "Expand-Archive -Path '%ZIP_FILE%' -DestinationPath '%MAVEN_USER_HOME%\dists' -Force"
@DEL "%ZIP_FILE%"

:RUN_MAVEN
@SET PATH=%MAVEN_HOME%\bin;%PATH%
@"%MAVEN_HOME%\bin\mvn.cmd" %*
