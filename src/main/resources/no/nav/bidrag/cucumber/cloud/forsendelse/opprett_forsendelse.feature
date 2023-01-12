# language: no
@bidrag-dokument-forsendelse-opprett-forsendelse
Egenskap: oppretter forsendelse med dokumenter

  Tester nais applikasjon bidrag-dokument-forsendelse

  Bakgrunn: Rest-tjeneste
    Gitt nais applikasjon 'bidrag-dokument-forsendelse'


  Scenario: Opprett forsendelse
    Når jeg bruker endpoint '/api/forsendelse' med json fra '/forsendelse/opprett_forsendelse.json'
    Så skal http status være 200
    Så skal lagre opprettet forsendelse respons
    Og forsendelse skal inneholde dokument med dokumentmal 'BI01S02' og status 'UNDER_REDIGERING'

