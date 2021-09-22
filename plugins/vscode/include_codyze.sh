#/bin/bash
rm -rf codyze*
cp ../build/distributions/codyze*.zip .
unzip codyze*.zip
mv codyze*/ codyze || true
