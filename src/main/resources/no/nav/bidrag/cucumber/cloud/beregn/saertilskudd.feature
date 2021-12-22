# language: no
@bidrag-beregn-saertilskudd-rest
Egenskap: bidrag-beregn-saertilskudd-rest

  Tester REST-api til bidrag-beregn-saertilskudd-rest

  Bakgrunn: Rest-tjeneste.
    Gitt nais applikasjon 'bidrag-beregn-saertilskudd-rest'

  Scenario: Sjekk om swagger-ui til bidrag-beregn-saertilskudd-rest er operativt
    Når det gjøres et kall til '/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config#'
    Så skal http status være 200
