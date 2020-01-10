FROM openjdk:11-jre-slim

EXPOSE 9000

WORKDIR /usr/local/codyze/

RUN apt-get update && apt-get install -y rlwrap
ADD build/distributions/codyze-*.tar .
RUN mv codyze-*/* . && rm -rf codyze-*

ENTRYPOINT sleep 0.1; /usr/bin/rlwrap /usr/local/codyze/bin/codyze
