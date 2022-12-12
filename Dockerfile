FROM eclipse-temurin:11.0.17_8-jre

LABEL org.opencontainers.image.authors="Fraunhofer AISEC <codyze@aisec.fraunhofer.de>"

# JVM monitoring and profiling
EXPOSE 9000

# extract versioned distribution
ADD codyze-cli/build/distributions/codyze-cli*.tar /usr/local/lib/
# make Codyze script accessible through PATH and create version independent directory
RUN ln -s /usr/local/lib/codyze-cli*/bin/codyze-cli /usr/local/bin/ \
    && ln -s /usr/local/lib/codyze-cli* /codyze-cli

# default location for project to be analyzed
WORKDIR /source

# default execution
ENTRYPOINT ["codyze-cli"]
CMD ["analyze", "--config", "./codyze.json"]
