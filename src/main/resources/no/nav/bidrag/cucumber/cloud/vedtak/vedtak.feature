# language: no
@bidrag-vedtak
Egenskap: bidrag-vedtak

  Tester REST-api til bidrag-vedtak

  Bakgrunn: Rest-tjeneste
    Gitt nais applikasjon 'bidrag-vedtak'
    Og nøkkel for testdata 'VEDTAK_NØKKEL'

  Scenario: Sjekk om swagger-ui til bidrag-vedtak er operativt
    Når det gjøres et kall til '/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config#'
    Så skal http status være 200

  Scenario: opprett vedtak
    Når jeg bruker endpoint '/vedtak/' med json fra '/vedtak/opprett_vedtak.json'
    Så skal http status være 200
    Så skal vedtakId lagres

  Scenario: hent vedtak
    Når det gjøres et kall til '/vedtak/{vedtakId}' med testdataparameter
    Så skal http status være 200
    Og responsen skal ikke være null
