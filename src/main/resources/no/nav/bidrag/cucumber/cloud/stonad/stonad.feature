# language: no
@bidrag-stonad
Egenskap: bidrag-stonad

  Tester REST-api til bidrag-stonad

  Bakgrunn: Rest-tjeneste.
    Gitt nais applikasjon 'bidrag-stonad'

  Scenario: Sjekk om swagger-ui til bidrag-stonad er operativt
    Når det gjøres et kall til '/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config#'
    Så skal http status være 200
