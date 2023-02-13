# language: no
@bidrag-grunnlag
Egenskap: bidrag-grunnlag

  Tester REST-api til bidrag-grunnlag

  Bakgrunn: Rest-tjeneste
    Gitt nais applikasjon 'bidrag-grunnlag'
    Og nøkkel for testdata 'GRUNNLAG_NØKKEL'

  Scenario: Sjekk om swagger-ui til bidrag-grunnlag er operativt
    Når det gjøres et kall til '/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config#'
    Så skal http status være 200

  Scenario: opprett grunnlagspakke
    Når jeg bruker endpoint '/grunnlagspakke' med json:
          """
           {
            "formaal": "FORSKUDD",
            "opprettetAv": "X123456"
           }
          """
    Så skal http status være 200
    Så skal grunnlagspakkeId lagres

  Scenario: hent grunnlagspakke
    Når det gjøres et kall til '/grunnlagspakke/{grunnlagspakkeId}' med testdataparameter
    Så skal http status være 200