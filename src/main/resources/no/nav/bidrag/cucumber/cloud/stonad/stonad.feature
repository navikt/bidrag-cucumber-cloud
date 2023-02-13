# language: no
@bidrag-stonad
Egenskap: bidrag-stonad

  Tester REST-api til bidrag-stonad

  Bakgrunn: Rest-tjeneste
    Gitt nais applikasjon 'bidrag-stonad'

  Scenario: Sjekk om swagger-ui til bidrag-stonad er operativt
    Når det gjøres et kall til '/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config#'
    Så skal http status være 200

  Scenario: hent stønad
    Når jeg bruker endpoint '/hent-stonad' med json:
          """
           {
            "kravhaverId": "11111111111",
            "sakId": "0809389",
            "skyldnerId": "55555555555",
            "type": "BIDRAG"
           }
          """
    Og jeg venter i to sekunder slik at vedtakhendelsen kan bli behandlet
    Så skal http status være 200
