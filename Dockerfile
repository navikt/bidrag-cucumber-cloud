FROM maven:3.8.1-openjdk-16
LABEL maintainer="Team Bidrag" \
      email="bidrag@nav.no"

COPY ./settings.xml /usr/share/maven/ref/
COPY ./src/ ./src/
COPY ./apps ./apps/
COPY ./pom.xml .
COPY ./integrationInput.json .

RUN mvn -B -f /pom.xml -s /usr/share/maven/ref/settings.xml clean install -DskipTests

EXPOSE 8080

ENTRYPOINT mvn -f /pom.xml -s /usr/share/maven/ref/settings.xml exec:java \
        -DINTEGRATION_INPUT=/integrationInput.json                        \
        -Dexec.classpathScope=test                                        \
        -Dexec.mainClass=io.cucumber.core.cli.Main                        \
        -Dexec.args="src/test/resources/no/nav/bidrag/cucumber/cloud --glue no.nav.bidrag.cucumber.cloud"
