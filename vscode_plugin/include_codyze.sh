#/bin/bash
rm -rf codyze*
cp ../codyze-v2/build/distributions/codyze-v2*.zip .
unzip codyze*.zip
mv codyze*/ codyze || true
