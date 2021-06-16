FROM maven:3.8.1-openjdk-16-slim
LABEL maintainer="Team Bidrag" \
      email="bidrag@nav.no"

# COPY ./scripts/settings.xml /root/.m2/.
COPY ./src .
COPY ./pom.xml .

ENTRYPOINT mvn exec:java                           \
        -Dexec.classpathScope=test                 \
        -Dexec.mainClass=io.cucumber.core.cli.Main \
        -Dexec.args="src/test/resources/no/nav/bidrag/cucumber/cloud --glue no.nav.bidrag.cucumber.cloud"
