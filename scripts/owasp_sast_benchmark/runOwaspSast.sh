#!/bin/sh

# ################################################################
#
# Downloads latest OWASP SAST benchmark and runs Codyze against it
#
# ################################################################

WORKING_DIR="$(dirname "$(readlink -f "$0")")/owasp_sast"

if  [ ! -d $WORKING_DIR ]; then
	echo "Downloading OWASP SAST test suite"
	wget https://github.com/OWASP/Benchmark/archive/master.zip
	echo "Unzipping ..."
	unzip master.zip -d $WORKING_DIR
fi

export JAVA_OPTS="$JAVA_OPTS --add-opens=java.base/java.lang=ALL-UNNAMED"
export JAVA_OPTS="$JAVA_OPTS --add-opens=java.base/java.lang.reflect=ALL-UNNAMED"
export JAVA_OPTS="$JAVA_OPTS --add-opens=java.base/java.util=ALL-UNNAMED"
export JAVA_OPTS="$JAVA_OPTS --add-opens=java.base/java.io=ALL-UNNAMED"

JAVA_OPTS=$JAVA_OPTS ../../build/install/codyze/bin/codyze -c -s=$WORKING_DIR/Benchmark-master/src/main/java/ -m=../../../src/test/resources/mark_java/
