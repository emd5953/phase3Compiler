del *.class
java -jar jflex-1.6.1.jar Lexer.flex
javac *.java

java Program ../samples/fail_01.minc   > ../samples/output_fail_01.txt
java Program ../samples/fail_01b.minc  > ../samples/output_fail_01b.txt
java Program ../samples/fail_02.minc   > ../samples/output_fail_02.txt
java Program ../samples/fail_02a.minc  > ../samples/output_fail_02a.txt
java Program ../samples/fail_02b.minc  > ../samples/output_fail_02b.txt
java Program ../samples/fail_02c.minc  > ../samples/output_fail_02c.txt
java Program ../samples/fail_02d.minc  > ../samples/output_fail_02d.txt
java Program ../samples/fail_02e.minc  > ../samples/output_fail_02e.txt
java Program ../samples/fail_03.minc   > ../samples/output_fail_03.txt
java Program ../samples/fail_03b.minc  > ../samples/output_fail_03b.txt
java Program ../samples/fail_04.minc   > ../samples/output_fail_04.txt
java Program ../samples/fail_05.minc   > ../samples/output_fail_05.txt
java Program ../samples/fail_06.minc   > ../samples/output_fail_06.txt
java Program ../samples/fail_06b.minc  > ../samples/output_fail_06b.txt
java Program ../samples/fail_07.minc   > ../samples/output_fail_07.txt
java Program ../samples/fail_08.minc   > ../samples/output_fail_08.txt
java Program ../samples/fail_08b.minc  > ../samples/output_fail_08b.txt
java Program ../samples/fail_09.minc   > ../samples/output_fail_09.txt
java Program ../samples/fail_09b.minc  > ../samples/output_fail_09b.txt
java Program ../samples/fail_10.minc   > ../samples/output_fail_10.txt
java Program ../samples/fail_11.minc   > ../samples/output_fail_11.txt
java Program ../samples/fail_12.minc   > ../samples/output_fail_12.txt
java Program ../samples/fail_13.minc   > ../samples/output_fail_13.txt
java Program ../samples/fail_14.minc   > ../samples/output_fail_14.txt
java Program ../samples/fail_14b.minc  > ../samples/output_fail_14b.txt
java Program ../samples/fail_15.minc   > ../samples/output_fail_15.txt
java Program ../samples/fail_15b.minc  > ../samples/output_fail_15b.txt
java Program ../samples/fail_15c.minc  > ../samples/output_fail_15c.txt

java Program ../samples/succ_01.minc   > ../samples/output_succ_01.txt
java Program ../samples/succ_02.minc   > ../samples/output_succ_02.txt
java Program ../samples/succ_03.minc   > ../samples/output_succ_03.txt
java Program ../samples/succ_04.minc   > ../samples/output_succ_04.txt
java Program ../samples/succ_05.minc   > ../samples/output_succ_05.txt
java Program ../samples/succ_06.minc   > ../samples/output_succ_06.txt
java Program ../samples/succ_07.minc   > ../samples/output_succ_07.txt
java Program ../samples/succ_08.minc   > ../samples/output_succ_08.txt
java Program ../samples/succ_09.minc   > ../samples/output_succ_09.txt
java Program ../samples/succ_10.minc   > ../samples/output_succ_10.txt
java Program ../samples/succ_11.minc   > ../samples/output_succ_11.txt
java Program ../samples/succ_12.minc   > ../samples/output_succ_12.txt
java Program ../samples/succ_13.minc   > ../samples/output_succ_13.txt
java Program ../samples/succ_14.minc   > ../samples/output_succ_14.txt
java Program ../samples/succ_15.minc   > ../samples/output_succ_15.txt
java Program ../samples/succ_16.minc   > ../samples/output_succ_16.txt
