FROM openjdk:11-jre-slim

EXPOSE 9000

WORKDIR /usr/local/cpganalysisserver/

ADD build/distributions/cpganalysisserver-*.tar .
RUN mv cpg-*/* . && rm -rf cpg-*

ENTRYPOINT ["/usr/local/cpganalysisserver/bin/cpganalysisserver"]
