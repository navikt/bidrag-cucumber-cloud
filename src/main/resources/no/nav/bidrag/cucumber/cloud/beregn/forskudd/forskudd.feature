# language: no
@bidrag-beregn-forskudd-rest
Egenskap: bidrag-beregn-forskudd-rest

  Tester REST API til endepunkt i bidrag-beregn-forskudd-rest.

  Bakgrunn: Rest-tjeneste.
    Gitt nais applikasjon 'bidrag-beregn-forskudd-rest'

  Scenario: Sjekk at swagger-ui er operativt
    Når det gjøres et kall til '/swagger-ui/index.html?configUrl=/bidrag-beregn-forskudd-rest/v3/api-docs/swagger-config#'
    Så skal http status være 200

  Scenario: beregn forskudd
    Når jeg bruker endpoint '/beregn/forskudd' med json fra 'forskudd/forskudd_eksempel.json'
    Så skal http status være 200
    Og responsen skal inneholde beløpet '2090' under stien '$.beregnetForskuddPeriodeListe[0].resultat.belop'
    Og responsen skal inneholde resultatkoden 'FORHOYET_FORSKUDD_11_AAR_125_PROSENT' under stien '$.beregnetForskuddPeriodeListe[0].resultat.kode'
