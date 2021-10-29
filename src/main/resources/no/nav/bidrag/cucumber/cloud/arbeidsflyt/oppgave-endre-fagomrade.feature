# language: no
@arbeidsflyt-endre-fagomrade
Egenskap: en journalpost endrer fagområde

  Tester nais applikasjon bidrag-arbeidsflyt
  Applikasjonen reagerer på at en journalpost endrer fagområde

  Bakgrunn: En oppgave lagret via oppgave api
    Gitt nais applikasjon 'oppgave'
    Og en journalpostHendelse med journalpostId 1010101010 og fagområde 'BID'
    Og at det finnes en oppgave under behandling

  Scenario: Ikke ferdigstill oppgaver når journalpost endres til internt fagområde (BID -> FAR og vice versa)
    Når hendelsen opprettes med fagområde 'FAR'
    Og jeg søker etter opprettet oppgave på fagområde 'BID', maks 3 ganger
    Så skal jeg finne oppgave i søkeresultatet

  Scenario: Ferdigstill oppgaver når journalpost endres til eksternt fagområde
    Når hendelsen opprettes med fagområde 'AAREG'
    Og jeg søker etter oppgaver på fagområde 'BID' etter behandling av hendelse
    Så skal jeg ikke finne oppgave i søkeresultatet
