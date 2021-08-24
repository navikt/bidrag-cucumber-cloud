# language: no
@bidrag-arbeidsflyt
Egenskap: bidrag-arbeidsflyt: AVVIK_ENDRE_FAGOMRADE

  Tester nais applikasjon bidrag-arbeidsflyt
  Applikasjonen reagerer på kafka hendelser og manipulerer oppgaver etter hendelse

  Bakgrunn: En oppgave lagret via oppgave api
    Gitt nais applikasjon 'oppgave'
    Og at en oppgave opprettes for 'AVVIK_ENDRE_FAGOMRADE' med journalpostId 1010101010 og tema 'BID'

  Scenario: Ikke ferdigstill oppgaver når journalpost bytter til internt fagområde (BID -> FAR og vice versa)
    Når det opprettes en journalposthendelse - 'AVVIK_ENDRE_FAGOMRADE' - for endring av fagområde fra 'BID' til 'FAR'
    Og jeg søker etter oppgave opprettet for 'AVVIK_ENDRE_FAGOMRADE' på tema 'BID'
    Så skal jeg finne oppgaven i søkeresultatet

  Scenario: Ferdigstill oppgaver når journalpost bytter til eksternt fagområde
    Når det opprettes en journalposthendelse - 'AVVIK_ENDRE_FAGOMRADE' - for endring av fagområde fra 'BID' til 'AAREG'
    Og jeg søker etter oppgave opprettet for 'AVVIK_ENDRE_FAGOMRADE' på tema 'BID'
    Så skal jeg ikke finne oppgaven i søkeresultatet

