# language: no
@bidrag-stonad-vedtak
Egenskap: bidrag-stonad-vedtak

  Forbereder test av REST-api til bidrag-stonad

  Bakgrunn: Rest-tjeneste
    Gitt nais applikasjon 'bidrag-vedtak'

  Scenario: opprett vedtak
    Når jeg bruker endpoint '/vedtak/' med json fra '/vedtak/opprett_vedtak.json'
    Så skal http status være 200
