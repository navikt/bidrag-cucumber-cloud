FROM library/maven:3.8.1-openjdk-16
LABEL maintainer="Team Bidrag" \
      email="teambidrag@nav.no"

COPY ./settings.xml /usr/share/maven/ref/
COPY ./src/ ./src/
COPY ./pom.xml .

RUN mvn -B -f /pom.xml -s /usr/share/maven/ref/settings.xml install -DskipTests

# download all dependencies to the docker image
RUN mvn -f /pom.xml -s /usr/share/maven/ref/settings.xml exec:java  \
        -Dexec.classpathScope=test                                  \
        -Dexec.mainClass=no.nav.bidrag.cucumber.BidragCucumberCloud || true

EXPOSE 8080

ENTRYPOINT echo "TestUser    : $TEST_USER" &&                                    \
                 echo "SANITY_CHECK: $SANITY_CHECK" &&                           \
                 echo "INGRESS@TAGS: $INGRESSES_FOR_TAGS" &&                     \
                 mvn -f /pom.xml -s /usr/share/maven/ref/settings.xml exec:java  \
                     -DTEST_USER=$TEST_USER                                      \
                     -DTEST_AUTH=$TEST_AUTH                                      \
                     -DSANITY_CHECK=$SANITY_C                                    \
                     -Dexec.classpathScope=test                                  \
                     -Dexec.mainClass=no.nav.bidrag.cucumber.BidragCucumberCloud \
                     -Dexec.args=$INGRESSES_FOR_TAGS
