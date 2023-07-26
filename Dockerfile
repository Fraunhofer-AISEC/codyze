FROM eclipse-temurin:17.0.8_7-jre

LABEL org.opencontainers.image.authors="Fraunhofer AISEC <codyze@aisec.fraunhofer.de>"

# JVM monitoring and profiling
EXPOSE 9000

# extract versioned distribution
ADD codyze-cli/build/distributions/codyze*.tar /usr/local/lib/
# make Codyze script accessible through PATH and create version independent directory
RUN ln -s /usr/local/lib/codyze*/bin/codyze /usr/local/bin/ \
    && ln -s /usr/local/lib/codyze* /codyze

# default location for project to be analyzed
WORKDIR /source

# default execution
ENTRYPOINT ["codyze"]
CMD ["analyze", "--config", "./codyze.json"]
