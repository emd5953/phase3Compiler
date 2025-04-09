@echo off
REM ----- Check for Lexer.flex and generate lexer if it exists -----
if exist "Lexer.flex" (
    echo Generating lexer from Lexer.flex using jflex-1.6.1.jar...
    java -jar jflex.jar Lexer.flex
    if errorlevel 1 (
        echo Lexer generation failed. Exiting.
        exit /b 1
    )
)

REM ----- Create class directory if it doesn't exist -----
if not exist "class" mkdir class

echo Compiling Java sources...
javac -d class *.java
if errorlevel 1 (
    echo Compilation failed. Exiting.
    exit /b 1
)

REM ----- Create actual_outputs folder (one level up) if it doesn't exist -----
if not exist "..\actual_outputs" mkdir ..\actual_outputs

REM Define the testcases directory relative to the src folder.
set "TEST_DIR=..\testcases"

REM ----------------------------
REM FAIL tests: results in 4 columns
REM ----------------------------
set "fail_line="
set /a fail_count=0

echo FAIL tests:

REM Loop through test identifiers for FAIL tests.
for %%i in (01 02 03 04 05 06 07 08 09 10) do (
    if "%%i"=="03" (
        call :processTest fail_03a
        call :processTest fail_03b
    ) else if "%%i"=="05" (
        call :processTest fail_05a
        call :processTest fail_05b
    ) else if "%%i"=="08" (
        call :processTest fail_08a
        call :processTest fail_08b
    ) else (
        call :processTest fail_%%i
    )
)

if defined fail_line echo %fail_line%

REM ----------------------------
REM SUCCESS tests: results in 4 columns
REM ----------------------------
set "succ_line="
set /a succ_count=0

echo.
echo SUCCESS tests:

for %%i in (01 02 03 04 05 06 07 08 09 10) do (
    call :processTestSucc succ_%%i
)

if defined succ_line echo %succ_line%

exit /b 0

:processTest
REM %1 is the test identifier (e.g., fail_03a)
set "ident=%1"
set "testfile=%TEST_DIR%\%ident%.minc"
set "expected=%TEST_DIR%\output_%ident%.txt"

REM Run the program and capture output (both stdout and stderr)
java -cp "class" Program "%testfile%" > "..\actual_outputs\%ident%.txt" 2>&1

REM Use fc for file comparison (quiet mode)
fc "..\actual_outputs\%ident%.txt" "%expected%" >nul
if errorlevel 1 (
    set "result=%ident%: FAIL"
) else (
    set "result=%ident%: PASS"
)

call :accumulateFail "%result%"
goto :eof

:accumulateFail
REM Append the result to a running line; print it every 4 entries.
set /a fail_count+=1
if defined fail_line (
    set "fail_line=%fail_line%    %~1"
) else (
    set "fail_line=%~1"
)
if %fail_count% geq 4 (
    echo %fail_line%
    set "fail_line="
    set /a fail_count=0
)
goto :eof

:processTestSucc
REM %1 is the test identifier (e.g., succ_01)
set "ident=%1"
set "testfile=%TEST_DIR%\%ident%.minc"
set "expected=%TEST_DIR%\output_%ident%.txt"

java -cp "class" Program "%testfile%" > "..\actual_outputs\%ident%.txt" 2>&1
fc "..\actual_outputs\%ident%.txt" "%expected%" >nul
if errorlevel 1 (
    set "result=%ident%: FAIL"
) else (
    set "result=%ident%: PASS"
)

call :accumulateSucc "%result%"
goto :eof

:accumulateSucc
set /a succ_count+=1
if defined succ_line (
    set "succ_line=%succ_line%    %~1"
) else (
    set "succ_line=%~1"
)
if %succ_count% geq 4 (
    echo %succ_line%
    set "succ_line="
    set /a succ_count=0
)
goto :eof
