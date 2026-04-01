@echo off
echo Downloading Maven...
bitsadmin /transfer "MavenDownload" https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip "%TEMP%\maven.zip"
echo Extracting Maven...
powershell -Command "Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%TEMP%\maven'"
set MAVEN_HOME=%TEMP%\maven\apache-maven-3.9.6
set PATH=%MAVEN_HOME%\bin;%PATH%
echo Maven installed temporarily.
mvn exec:java