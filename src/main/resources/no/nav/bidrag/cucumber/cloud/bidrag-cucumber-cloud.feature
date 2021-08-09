# language: no
@bidrag-cucumber-cloud
Egenskap: bidrag-cucumber-cloud

  Tester REST API med swagger i bidrag-cucumber-cloud
  URLer til tags hentes fra json-property, ingressesForTags

  Bakgrunn: Rest-tjeneste.
    Gitt nais applikasjon 'bidrag-cucumber-cloud'

  Scenario: Sjekk at swagger-ui er operativt
    Når det gjøres et kall til '/swagger-ui/index.html?configUrl=/bidrag-cucumber-cloud/v3/api-docs/swagger-config#/'
    Så skal http status være 200
