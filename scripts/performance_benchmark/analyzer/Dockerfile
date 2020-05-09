FROM gradle:6.0-jdk11 as build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
COPY codyze_m2 /root/.m2/repository/de/fraunhofer/aisec/codyze
RUN gradle clean fatJar --no-daemon

FROM openjdk:11-jre-slim

EXPOSE 8080 9010

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*-all.jar /app/app.jar
ENTRYPOINT ["java", \
"-Dcom.sun.management.jmxremote", \
"-Dcom.sun.management.jmxremote.port=9010", \
"-Dcom.sun.management.jmxremote.local.only=false", \
"-Dcom.sun.management.jmxremote.authenticate=false", \
"-Dcom.sun.management.jmxremote.ssl=false", \
"-Dcom.sun.management.jmxremote.rmi.port=9010", \
"-Djava.rmi.server.hostname=localhost", \
"-jar", \
 "/app/app.jar"]