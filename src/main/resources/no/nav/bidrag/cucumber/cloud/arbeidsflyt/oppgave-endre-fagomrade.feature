# language: no
@arbeidsflyt-endre-fagomrade
Egenskap: en journalpost endrer fagområde

  Tester nais applikasjon bidrag-arbeidsflyt
  Applikasjonen reagerer på at en journalpost endrer fagomraåde og muligens ferdigstiller oppgaver

  Bakgrunn: En oppgave lagret via oppgave api
    Gitt nais applikasjon 'oppgave'
    Og en journalpostHendelse med journalpostId 1010101010 og fagområde 'BID'
    Og at det finnes en oppgave under behandling

  Scenario: Ikke ferdigstill oppgaver når journalpost bytter til internt fagområde (BID -> FAR og vice versa)
    Når hendelsen opprettes med fagområde 'FAR'
    Og jeg venter i et sekund slik at hendelse blir behandlet
    Og jeg søker etter oppgaven på fagområde 'BID'
    Så skal jeg finne oppgaven i søkeresultatet

  Scenario: Ferdigstill oppgaver når journalpost bytter til eksternt fagområde
    Når hendelsen opprettes med fagområde 'AAREG'
    Og jeg venter i et sekund slik at hendelse blir behandlet
    Og jeg søker etter oppgaven på fagområde 'BID'
    Så skal jeg ikke finne oppgaven i søkeresultatet

