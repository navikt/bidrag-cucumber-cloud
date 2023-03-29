# language: no
@bidrag-dokument-forsendelse-opprett-forsendelse
Egenskap: oppretter forsendelse med dokumenter

  Tester nais applikasjon bidrag-dokument-forsendelse

  Bakgrunn: Rest-tjeneste
    Gitt nais applikasjon 'bidrag-dokument-forsendelse'
    Og nøkkel for testdata 'OPPRETT_FORSENDELSE'

  Scenario: Opprett forsendelse
    Når jeg bruker endpoint '/api/forsendelse' med json fra '/forsendelse/opprett_forsendelse.json'
    Så skal http status være 200
    Så skal lagre opprettet forsendelse detaljer
    Og forsendelse inneholder 1 dokumenter
    Og forsendelse inneholder dokument med dokumentmal 'BI01S02' og status 'UNDER_REDIGERING'

  Scenario: Legg til dokument
    Når jeg bruker endpoint '/api/forsendelse/{forsendelseId}/dokument' med json fra '/forsendelse/legg_til_joark_dokument.json'
    Så skal http status være 200
    Og forsendelse inneholder 2 dokumenter

  Scenario: Slett dokument
    Når jeg kaller endepunkt '/api/forsendelse/{forsendelseId}/{dokumentreferanse2}' med http metode 'DELETE'
    Så skal http status være 200
    Og forsendelse inneholder 1 dokumenter

