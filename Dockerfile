FROM navikt/java:16
LABEL maintainer="Team Bidrag" \
      email="nav.ikt.prosjekt.og.forvaltning.bidrag@nav.no"

COPY ./target/bidrag-cucumber-nais-jar-with-dependencies.jar app.jar
