@echo off
setlocal

REM Set paths for the JDK installation, source files, and JARs
SET JDK_URL=https://download.oracle.com/java/17/latest/jdk-17_windows-x64_bin.zip
SET JDK_DIR=%~dp0local_jdk
SET OCTANE_JAR=%~dp0\lib\Octane.jar
SET SOURCE_FILE=%~dp0\src\TagReportListenerImplementation.java
SET CLASS_FILE=%~dp0TagReportListenerImplementation.class
SET INTERFACES_JAR=%~dp0\lib\interfaces.jar
SET TEMP_DIR=%~dp0temp

REM Create a temporary directory for the JDK download
IF NOT EXIST %TEMP_DIR% (
    mkdir %TEMP_DIR%
)

REM Download the JDK (skip if already downloaded)
IF NOT EXIST %TEMP_DIR%\jdk.zip (
    echo Downloading JDK...
    powershell -Command "Invoke-WebRequest -Uri %JDK_URL% -OutFile %TEMP_DIR%\jdk.zip"
)

REM Extract the JDK (skip if already extracted)
IF NOT EXIST %JDK_DIR% (
    echo Extracting JDK...
    powershell -Command "Expand-Archive -Path %TEMP_DIR%\jdk.zip -DestinationPath %JDK_DIR% -Force"
)

REM Set the JAVA_HOME and PATH for the local JDK
SET JAVA_HOME=%JDK_DIR%\jdk-17
SET PATH=%JAVA_HOME%\bin;%PATH%

REM Compile the Java file using the Octane.jar
javac -cp %OCTANE_JAR% %SOURCE_FILE%

REM Check if the compilation was successful
IF EXIST %CLASS_FILE% (
    echo Compilation successful. Moving the .class file to interfaces.jar...

    REM Update interfaces.jar with the new .class file
    jar cf %INTERFACES_JAR% %CLASS_FILE%

    echo Update successful. Cleaning up...

    REM Optionally, delete the .class file after adding it to the jar
    del %CLASS_FILE%

    echo Done.
) ELSE (
    echo Compilation failed. .class file not found.
)

REM Clean up the temporary directory
IF EXIST %TEMP_DIR% (
    rmdir /S /Q %TEMP_DIR%
)

pause
endlocal
