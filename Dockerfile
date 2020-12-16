FROM navikt/java:15
LABEL maintainer="Team Bidrag" \
      email="nav.ikt.prosjekt.og.forvaltning.bidrag@nav.no"

COPY ./target/bidrag-cucumber-nais.jar app.jar
