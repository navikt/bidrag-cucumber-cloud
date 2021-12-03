# language: no
@bidrag-vedtak
Egenskap: bidrag-vedtak

  Tester REST-api til bidrag-vedtak

  Bakgrunn: Rest-tjeneste.
    Gitt nais applikasjon 'bidrag-vedtak'

  Scenario: Sjekk om swagger-ui til bidrag-vedtak er operativt
    Når det gjøres et kall til '/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config#'
    Så skal http status være 200
