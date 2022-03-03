FROM openjdk:11-jre-slim

LABEL org.opencontainers.image.authors="Fraunhofer AISEC <codyze@aisec.fraunhofer.de>" \
      org.opencontainers.image.licenses="Apache-2.0" \
      org.opencontainers.image.source="https://github.com/Fraunhofer-AISEC/codyze" \
      org.opencontainers.image.vendor="Fraunhofer AISEC" \
      org.opencontainers.image.base.name="openjdk:11-jre-slim"

# JVM monitoring and profiling
EXPOSE 9000

# extract versioned distribution
ADD build/distributions/codyze-*.tar /usr/local/lib/
# make Codyze script accessible through PATH and create version independent directory
RUN ln -s /usr/local/lib/codyze-*/bin/codyze /usr/local/bin/ \
    && ln -s /usr/local/lib/codyze-* /codyze

# default location for project to be analyzed
WORKDIR /source

# default execution
ENTRYPOINT ["codyze"]
CMD ["-c", "-m", "/codyze/mark/", "-s", "."]
