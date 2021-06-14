# language: no
@bidrag-dokument
Egenskap: bidrag-dokument: applikasjon
  Applikasjonen bidrag-dokument er klar for bruk

  Bakgrunn: Felles for scenarioer
    Gitt nais applikasjon 'bidrag-dokument'

  Scenario: skal kunne hente informasjon om applikasjonens status
    Når jeg kaller helsetjenesten
    Så skal http status være 200
    Og responsen skal inneholde 'status' = 'UP'

  Scenario: skal kunne bruke en operasjon med sikkerhet satt opp med gyldig token
    Når det gjøres et kall til '/sak/0000003/journal?fagomrade=BID'
    Så skal http status ikke være 401 eller 403

  Scenario: skal ikke kunne bruke en operasjon med sikkerhet satt opp uten gyldig token
    Når det mangler sikkerhetstoken i HttpRequest
    Og det gjøres et kall til '/sak/0000003/journal?fagomrade=BID'
    Så skal http status være 401
