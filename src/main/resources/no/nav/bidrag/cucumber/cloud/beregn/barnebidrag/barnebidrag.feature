# language: no
@bidrag-beregn-barnebidrag-rest
Egenskap: bidrag-beregn-barnebidrag-rest

  Tester REST API til endepunkt i bidrag-beregn-barnebidrag-rest.

  Bakgrunn: Rest-tjeneste.
    Gitt nais applikasjon 'bidrag-beregn-barnebidrag-rest'

  Scenario: Sjekk at swagger-ui er operativt
    Når det gjøres et kall til '/swagger-ui/index.html?configUrl=/bidrag-beregn-barnebidrag-rest/v3/api-docs/swagger-config#'
    Så skal http status være 200

  Scenario: beregn barnebidrag
    Når jeg bruker endpoint '/beregn/barnebidrag' med json fra 'barnebidrag/barnebidrag_eksempel.json'
    Så skal http status være 200
    Og responsen skal inneholde beløpet '3490' under stien '$.beregnetBarnebidragPeriodeListe[0].resultat.belop'
    Og responsen skal inneholde resultatkoden 'KOSTNADSBEREGNET_BIDRAG' under stien '$.beregnetBarnebidragPeriodeListe[0].resultat.kode'

  Scenario: beregn barnebidrag med forholdsmessig fordeling
    Når jeg bruker endpoint '/beregn/forholdsmessigfordeling' med json fra 'barnebidrag/forholdsmessig_fordeling_eksempel.json'
    Så skal http status være 200
    Og responsen skal inneholde beløpet '2060' under stien '$.beregnetForholdsmessigFordelingPeriodeListe[0].resultat.belop'
    Og responsen skal inneholde resultatkoden 'FORHOLDSMESSIG_FORDELING_BIDRAGSBELOP_ENDRET' under stien '$.beregnetForholdsmessigFordelingPeriodeListe[0].resultat.kode'
