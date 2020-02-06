#!/bin/sh

# ################################################################
#
# Downloads latest OWASP SAST benchmark and runs Codyze against it
#
# (C) Fraunhofer AISEC 2020, Julian Schütte
#
# ################################################################

WORKING_DIR="$(dirname "$(readlink -f "$0")")/owasp_sast"

if  [ ! -d $WORKING_DIR ]; then
	echo "Downloading OWASP SAST test suite"
	wget https://github.com/OWASP/Benchmark/archive/master.zip
	echo "Unzipping ..."
	unzip master.zip -d $WORKING_DIR
fi

JAVA_OPTS="$JAVA_OPTS --add-opens=java.base/java.lang=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens=java.base/java.lang.reflect=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens=java.base/java.util=ALL-UNNAMED"
JAVA_OPTS="$JAVA_OPTS --add-opens=java.base/java.io=ALL-UNNAMED"

# Source is whole OWASP benchmark by default, otherwise file given as input argument
SOURCE=$WORKING_DIR/Benchmark-master/src/main/java/
if [ ! -z "$1" ]
then
	SOURCE=$1
fi
echo "Running benchmark against $SOURCE"
JAVA_OPTS=$JAVA_OPTS ../../build/install/codyze/bin/codyze -c --timeout=1200 -s=$SOURCE -m=../../../src/test/resources/mark_java/
