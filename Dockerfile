FROM library/maven:3.8.1-openjdk-16
LABEL maintainer="Team Bidrag" \
      email="teambidrag@nav.no"

COPY ./settings.xml /usr/share/maven/ref/
COPY ./src/ ./src/
COPY ./pom.xml .

RUN mvn -B -f /pom.xml -s /usr/share/maven/ref/settings.xml install -DskipTests

EXPOSE 8080

ENTRYPOINT mvn -f /pom.xml -s /usr/share/maven/ref/settings.xml exec:java \
        -Dexec.classpathScope=test                                        \
        -Dexec.mainClass=io.cucumber.core.cli.Main                        \
        -Dexec.args="src/test/resources/no/nav/bidrag/cucumber/cloud --glue no.nav.bidrag.cucumber.cloud"
