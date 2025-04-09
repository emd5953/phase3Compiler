#!/bin/bash

# Check if a lexer specification file exists and generate the lexer.
if [ -f "Lexer.flex" ]; then
    echo "Generating lexer from Lexer.flex using jflex-1.6.1.jar..."
    if ! java -jar jflex.jar Lexer.flex; then
        echo "Lexer generation failed. Exiting."
        exit 1
    fi
fi

# Create the 'class' directory if it doesn't exist.
mkdir -p class

echo "Compiling Java sources..."
if ! javac -d class *.java; then
    echo "Compilation failed. Exiting."
    exit 1
fi

# Create actual_outputs folder (one level up) if it doesn't exist.
mkdir -p ../actual_outputs

# Define the testcases directory relative to the src folder.
TEST_DIR="../testcases"

# ----------------------------
# FAIL tests: results in 4 columns
# ----------------------------
fail_line=""
fail_count=0

echo "FAIL tests:"

processTest() {
    local ident="$1"
    local testfile="$TEST_DIR/${ident}.minc"
    local expected="$TEST_DIR/output_${ident}.txt"
    java -cp "class" Program "$testfile" > "../actual_outputs/${ident}.txt" 2>&1
    # Compare the output using diff.
    if ! diff -q "../actual_outputs/${ident}.txt" "$expected" > /dev/null; then
        result="${ident}: FAIL"
    else
        result="${ident}: PASS"
    fi
    accumulateFail "$result"
}

accumulateFail() {
    ((fail_count++))
    fail_line="${fail_line}    $1"
    if [ "$fail_count" -ge 4 ]; then
        echo "$fail_line"
        fail_line=""
        fail_count=0
    fi
}

for i in 01 02 03 04 05 06 07 08 09 10; do
    if [ "$i" == "03" ]; then
        processTest "fail_03a"
        processTest "fail_03b"
    elif [ "$i" == "05" ]; then
        processTest "fail_05a"
        processTest "fail_05b"
    elif [ "$i" == "08" ]; then
        processTest "fail_08a"
        processTest "fail_08b"
    else
        processTest "fail_${i}"
    fi
done

if [ -n "$fail_line" ]; then
    echo "$fail_line"
fi

# ----------------------------
# SUCCESS tests: results in 4 columns
# ----------------------------
succ_line=""
succ_count=0

echo
echo "SUCCESS tests:"

processTestSucc() {
    local ident="$1"
    local testfile="$TEST_DIR/${ident}.minc"
    local expected="$TEST_DIR/output_${ident}.txt"
    java -cp "class" Program "$testfile" > "../actual_outputs/${ident}.txt" 2>&1
    if ! diff -q "../actual_outputs/${ident}.txt" "$expected" > /dev/null; then
        result="${ident}: FAIL"
    else
        result="${ident}: PASS"
    fi
    accumulateSucc "$result"
}

accumulateSucc() {
    ((succ_count++))
    succ_line="${succ_line}    $1"
    if [ "$succ_count" -ge 4 ]; then
        echo "$succ_line"
        succ_line=""
        succ_count=0
    fi
}

for i in 01 02 03 04 05 06 07 08 09 10; do
    processTestSucc "succ_${i}"
done

if [ -n "$succ_line" ]; then
    echo "$succ_line"
fi
