# language: no
@bidrag-beregn-barnebidrag-rest
Egenskap: bidrag-beregn-barnebidrag-rest

  Tester REST API til endepunkt i bidrag-beregn-barnebidrag-rest.
  URL hentes fra json-property, ingressesForTags.

  Bakgrunn: Rest-tjeneste.
    Gitt nais applikasjon 'bidrag-beregn-barnebidrag-rest'

  Scenario: Sjekk at swagger-ui er operativt
    Når det gjøres et kall til '/swagger-ui/index.html?configUrl=/bidrag-beregn-barnebidrag-rest/v3/api-docs/swagger-config#'
    Så skal http status være 200

  Scenario: beregn barnebidrag
    Når jeg bruker endpoint '/beregn/barnebidrag' med json fra 'barnebidrag/barnebidrag_eksempel.json'
    Så skal http status være 200
    Og responsen skal inneholde beløpet '3490' under stien '$.beregnBarnebidragResultat.resultatPeriodeListe[0].resultatBeregningListe[0].resultatBelop'
    Og responsen skal inneholde resultatkoden 'KOSTNADSBEREGNET_BIDRAG' under stien '$.beregnBarnebidragResultat.resultatPeriodeListe[0].resultatBeregningListe[0].resultatKode'

  Scenario: beregn barnebidrag med forholdsmessig fordeling
    Når jeg bruker endpoint '/beregn/forholdsmessigfordeling' med json fra 'barnebidrag/forholdsmessig_fordeling_eksempel.json'
    Så skal http status være 200
    Og responsen skal inneholde beløpet '2060' under stien '$.resultatPeriodeListe[0].resultatBeregningListe[0].resultatPerBarnListe[0].resultatBarnebidragBelop'
    Og responsen skal inneholde resultatkoden 'FORHOLDSMESSIG_FORDELING_BIDRAGSBELOP_ENDRET' under stien '$.resultatPeriodeListe[0].resultatBeregningListe[0].resultatPerBarnListe[0].resultatKode'
