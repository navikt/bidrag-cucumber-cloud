# language: no
@bidrag-cucumber-cloud
Egenskap: bidrag-cucumber-cloud

  Tester REST API til endepunkt i bidrag-cucumber-cloud.
  URLer til tags hentes fra json-property, ingressesForTags

  Bakgrunn: Rest-tjeneste.
    Gitt nais applikasjon 'bidrag-cucumber-cloud'

  Scenario: Sjekk at health endpoint er operativt
    Når jeg kaller helsetjenesten
    Så skal http status være 200
    Og header 'content-type' skal være 'application/json'
    Og responsen skal inneholde 'status' = 'UP'
