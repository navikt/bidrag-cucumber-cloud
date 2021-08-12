# language: no
@bidrag-arbeidsflyt
Egenskap: bidrag-arbeidsflyt

  Tester nais applikasjon bidrag-arbeidsflyt
  URLer til tags hentes fra json-property, ingressesForTags

  Bakgrunn: NAIS applikasjon.
    Gitt nais applikasjon 'bidrag-arbeidsflyt'

  Scenario: Sjekk at health endpoint er operativt
    Når jeg kaller helsetjenesten
    Så skal http status være 200
    Og header 'content-type' skal være 'application/json'
    Og responsen skal inneholde 'status' = 'UP'
