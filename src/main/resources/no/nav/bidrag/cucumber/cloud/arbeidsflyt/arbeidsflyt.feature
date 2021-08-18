# language: no
@bidrag-arbeidsflyt
Egenskap: bidrag-arbeidsflyt

  Tester nais applikasjon bidrag-arbeidsflyt
  Applikasjonen reagerer på kafka hendelser og manipulerer oppgaver etter hendelse

  Bakgrunn: En oppgave lagret via oppgave api
    Gitt nais applikasjon 'oppgave'
    Og at en oppgave opprettes med journalpostId 1010101010 og tema 'BID'

  Scenario: Ikke ferdigstill oppgaver når journalpost bytter til internt fagområde (BID -> FAR og vice versa)
    Når det opprettes en journalposthendelse - 'AVVIK_ENDRE_FAGOMRADE' - for endring av fagområde til 'FAR'
    Og jeg søker etter oppgave
    Så skal jeg finne oppgaven i søkeresultatet

  Scenario: Ferdigstill oppgaver når journalpost bytter til eksternt fagområde
    Når det opprettes en journalposthendelse - 'AVVIK_ENDRE_FAGOMRADE' - for endring av fagområde til 'AAREG'
    Og jeg søker etter oppgave
    Så skal jeg ikke finne oppgaven i søkeresultatet

