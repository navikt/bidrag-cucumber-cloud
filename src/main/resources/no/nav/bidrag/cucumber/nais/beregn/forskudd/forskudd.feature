# language: no
@bidrag-beregn-forskudd-rest
Egenskap: bidrag-beregn-forskudd-rest

  Tester REST API til endepunkt i bidrag-beregn-forskudd-rest.
  URLer til tjenester hentes via nais/nais.yaml og gjøres ved å navngi prosjektet som man skal
  kommunisere med (via REST).

  Bakgrunn: Rest-tjeneste.
    Gitt nais applikasjon 'bidrag-beregn-forskudd-rest'

  Scenario: Sjekk at health endpoint er operativt
    Når jeg kaller helsetjenesten
    Så skal http status være 200
    Og header 'content-type' skal være 'application/json'
    Og responsen skal inneholde 'status' = 'UP'

  Scenario:
    Når jeg bruker endpoint '/beregn/forskudd' med json fra 'forskudd/forskudd_eksempel.json'
    Så skal http status være 200
    Og responsen skal inneholde beløpet '1530.0' under stien '$.resultatPeriodeListe[0].resultatBeregning.resultatBelop'
    Og responsen skal inneholde resultatkoden 'INNVILGET_100_PROSENT' under stien '$.resultatPeriodeListe[0].resultatBeregning.resultatKode'

