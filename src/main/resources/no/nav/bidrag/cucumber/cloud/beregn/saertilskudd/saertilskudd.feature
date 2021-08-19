# language: no
@bidrag-beregn-saertilskudd-rest
Egenskap: bidrag-beregn-saertilskudd-rest

  Tester REST API til endepunkt i bidrag-beregn-saertilskudd-rest.

  Bakgrunn: Rest-tjeneste.
    Gitt nais applikasjon 'bidrag-beregn-saertilskudd-rest'

  Scenario: Sjekk at swagger-ui er operativt
    Når det gjøres et kall til '/swagger-ui/index.html?configUrl=/bidrag-beregn-saertilskudd-rest/v3/api-docs/swagger-config#'
    Så skal http status være 200

  Scenario: beregn særtilskudd
    Når jeg bruker endpoint '/beregn/saertilskudd' med json fra 'saertilskudd/saertilskudd_eksempel.json'
    Så skal http status være 200
    Og responsen skal inneholde beløpet '4242' under stien '$.beregnSaertilskuddResultat.resultatPeriodeListe[0].resultatBeregning.resultatBelop'
    Og responsen skal inneholde resultatkoden 'SAERTILSKUDD_INNVILGET' under stien '$.beregnSaertilskuddResultat.resultatPeriodeListe[0].resultatBeregning.resultatKode'
