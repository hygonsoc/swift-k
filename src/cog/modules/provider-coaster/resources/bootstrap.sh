BS=$1
LS=$2
EMD5=$3
ID=$5
H=$6
L=$7
B="coaster-bootstrap"

error() {
	echo $1
	echo $1 >>$L
	rm -f $DJ
	exit 1
}
find() {
	R=`eval which $1 2>>$L`
	CMD="which $1 1>/tmp/$ID 2>>$L"
	if [ "X$R" == "X" ] || [ ! -x $R ]; then
		/bin/bash -l -c "$CMD" >>$L
		R=`cat /tmp/$ID`
	fi
	echo "find $1 = $R" >>$L
	rm -f /tmp/$ID
	echo $R
}
if [ "$L" == "" ]; then
	L=~/$B-$ID.log 
fi
DJ=`mktemp /tmp/bootstrap.XXXXXX`
echo "BS: $BS" >>$L
WGET=`find wget`
if [ "X$WGET" == "X" ]; then
	WGET=`find curl`
	if [ "X$WGET" == "X" ]; then
		error "No wget or curl available"
	fi
	WGET="$WGET -O $DJ $BS/$B.jar >>$L 2>&1"
else
	WGET="$WGET -c -q $BS/$B.jar -O $DJ >>$L 2>&1"
fi
echo "-->$WGET<--" >>$L
eval $WGET
if [ "$?" != "0" ]; then
	error "Failed to download bootstrap jar from $BS"
fi
MD5SUM=`find gmd5sum`
if [ "X$MD5SUM" == "X" ]; then
	MD5SUM=`find md5sum`
	if [ "X$MD5SUM" == "X" ]; then
		error "No md5sum or gmd5sum found"
	fi
fi
AMD5=`$MD5SUM $DJ`
AAMD5=`eval echo \$\{AMD5:0:32\}`
echo "Expected checksum: $EMD5" >>$L
echo "Computed checksum: $AAMD5" >>$L
if [ "$AAMD5" != "$EMD5" ]; then
	error "Bootstrap jar checksum failed: $EMD5 != $AAMD5"
fi

JAVA=`find java`
if [ "X$JAVA" == "X" ]; then
	JAVA=$JAVA_HOME/bin/java
fi
echo "JAVA=$JAVA" >>$L
if [ -x $JAVA ]; then 
	CMD="$JAVA -Djava=\"$JAVA\" -DGLOBUS_TCP_PORT_RANGE=\"$GLOBUS_TCP_PORT_RANGE\" -DX509_USER_PROXY=\"$X509_USER_PROXY\" -DX509_CERT_DIR=\"$X509_CERT_DIR\" -DGLOBUS_HOSTNAME=\"$H\" -jar $DJ $BS $LS $ID"
	echo $CMD >>$L
	eval $CMD >>$L	
	EC=$?
	echo "EC: $EC" >>$L
	rm -f $DJ
	exit $EC
else
	error "Could not find a valid java executable"
fi
