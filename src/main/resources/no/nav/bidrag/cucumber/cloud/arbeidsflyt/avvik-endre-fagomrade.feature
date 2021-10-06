# language: no
@arbeidsflyt-endre-fagomrade
Egenskap: bidrag-arbeidsflyt: AVVIK_ENDRE_FAGOMRADE

  Tester nais applikasjon bidrag-arbeidsflyt
  Applikasjonen reagerer på kafka hendelser og manipulerer oppgaver etter hendelse

  Bakgrunn: En oppgave lagret via oppgave api
    Gitt nais applikasjon 'oppgave'
    Og hendelse 'AVVIK_ENDRE_FAGOMRADE' for journalpostId 1010101010 og tema 'BID'
    Og at det finnes en oppgave under behandling

  Scenario: Ikke ferdigstill oppgaver når journalpost bytter til internt fagområde (BID -> FAR og vice versa)
    Når hendelsen opprettes for endring av fagområde til 'FAR'
    Og jeg søker etter oppgaven
    Så skal jeg finne oppgaven i søkeresultatet

  Scenario: Ferdigstill oppgaver når journalpost bytter til eksternt fagområde
    Når hendelsen opprettes for endring av fagområde til 'AAREG'
    Og jeg søker etter oppgaven
    Så skal jeg ikke finne oppgaven i søkeresultatet

