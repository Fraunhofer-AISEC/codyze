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
