#!/bin/bash

for i in `ls *setup.sh`
do
    BASE=${i%.setup.sh};
    echo $BASE
    echo "Source file : $BASE.source.sh" 

    cat <<'EOF' > $BASE.check.sh
#!/bin/bash

rm -rf "dummy" *.out &> /dev/null
if [ "$KILL_JAVA" == "true" ];
then
    echo "Killing Java.."
    killall -u $USER java -9
fi

IN=`grep "PerformanceDiagnosticInputStream\ \[IN\]" $BASE*log | tail -n 1`
MEM=`grep "PerformanceDiagnosticInputStream\ \[MEM\]" $BASE*log`
OUT=`grep "PerformanceDiagnosticOutputStream\ \[OUT\]" $BASE*log | tail -n 1`

echo "====="
echo "IN  : $IN"
echo "MEM : $MEM"
echo "OUT : $OUT"
echo "====="

EOF

done;