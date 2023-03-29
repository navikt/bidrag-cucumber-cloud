# language: no
@bidrag-dokument-forsendelse-distribuer-forsendelse
Egenskap: Distribuer forsendelse med dokumenter

  Tester nais applikasjon bidrag-dokument-forsendelse

  Bakgrunn: Rest-tjeneste
    Gitt nais applikasjon 'bidrag-dokument'
    Og nøkkel for testdata 'DISTRIBUER_FORSENDELSE'

  Scenario: Opprett joark journalpost
    Når jeg bruker endpoint '/journalpost/JOARK' med json fra '/forsendelse/opprett_joark_journalpost.json'
    Så skal lagre opprettet journalpost detaljer

  Scenario: Opprett forsendelse
    Gitt nais applikasjon 'bidrag-dokument-forsendelse'
    Når jeg bruker endpoint '/api/forsendelse' med json:
          """
           {
            "gjelderIdent": "20527722013",
            "enhet": "4806",
            "saksnummer": "2300002",
            "mottaker": {
              "ident": "20527722013",
              "navn": "Navn Navnesen",
              "adresse": {
                "adresselinje1": "Buskerudgata",
                "adresselinje2": "H0505",
                "adresselinje3": "H0505",
                "postnummer": "3044",
                "poststed": "Drammen"
              }
            },
            "dokumenter": [
              {
                "tittel": "Tittel på fritekstbrev hoveddokument",
                "dokumentmalId": "BI01S02",
                "dokumentreferanse": "${joark_dokumentreferanse}",
                "journalpostId": "JOARK-${journalpostId}"
              }
            ],
            "journalposttype": "UTGÅENDE"
          }
        """
    Så skal lagre opprettet forsendelse detaljer
    Når jeg kaller endepunkt '/api/forsendelse/journal/distribuer/BIF-{forsendelseId}'
    Så skal http status være 200
    Og forsendelse inneholder joark journalpostid


