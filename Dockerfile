FROM openjdk:11-jre-slim

EXPOSE 9000

WORKDIR /usr/local/cpganalysisserver/

RUN apt-get update && apt-get install -y rlwrap
ADD build/distributions/cpganalysisserver-*.tar .
RUN mv cpganalysisserver-*/* . && rm -rf cpganalysisserver-*

ENTRYPOINT sleep 0.1; /usr/bin/rlwrap /usr/local/cpganalysisserver/bin/cpganalysisserver
