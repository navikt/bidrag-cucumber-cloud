# language: no
@bidrag-grunnlag
Egenskap: bidrag-grunnlag

  Tester REST-api til bidrag-grunnlag

  Bakgrunn: Rest-tjeneste.
    Gitt nais applikasjon 'bidrag-grunnlag'

  Scenario: Sjekk om swagger-ui til bidrag-grunnlag er operativt
    Når det gjøres et kall til '/swagger-ui/index.html?configUrl=/bidrag-grunnlag/v3/api-docs/swagger-config#'
    Så skal http status være 200
