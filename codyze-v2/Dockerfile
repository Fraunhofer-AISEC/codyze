FROM eclipse-temurin:11.0.16_8-jre

LABEL org.opencontainers.image.authors="Fraunhofer AISEC <codyze@aisec.fraunhofer.de>"

# JVM monitoring and profiling
EXPOSE 9000

# extract versioned distribution
ADD build/distributions/codyze-v2-*.tar /usr/local/lib/
# make Codyze script accessible through PATH and create version independent directory
RUN ln -s /usr/local/lib/codyze-v2-*/bin/codyze-v2 /usr/local/bin/ \
    && ln -s /usr/local/lib/codyze-v2-* /codyze-v2

# default location for project to be analyzed
WORKDIR /source

# default execution
ENTRYPOINT ["codyze-v2"]
CMD ["-c", "-m", "/codyze-v2/mark/", "-s", "."]
