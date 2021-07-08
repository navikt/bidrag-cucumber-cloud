FROM navikt/java:16
LABEL maintainer="Team Bidrag" \
      email="nav.ikt.prosjekt.og.forvaltning.bidrag@nav.no"

COPY src/main/resources/no features/no
COPY ./target/*-jar-with-dependencies.jar app.jar
