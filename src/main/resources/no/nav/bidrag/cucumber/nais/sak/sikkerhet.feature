# language: no
@bidrag-sak
Egenskap: bidrag-sak med Azure-token
  Tester bidrag-sak

  Scenario: Teste bidrag-sak med token fra Azure
    Gitt nais applikasjon 'bidrag-sak'
    Og en sak med saksnr "1900000"
    Når jeg henter denne saken
    Så skal http status være 200
