# language: no
Egenskap:  bidrag-sak med Azure-token
  Tester bidrag-sak

  Scenario: Teste bidrag-sak med token fra Azure
    Gitt en sak med saksnr "1900000"
    Når jeg henter denne saken
    Så skal resultatet være 200


