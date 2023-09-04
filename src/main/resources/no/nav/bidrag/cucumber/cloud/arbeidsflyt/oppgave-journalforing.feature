# language: no
@arbeidsflyt-journalforing
Egenskap: en journalpost og journalføringsoppgave

  Tester nais applikasjon bidrag-arbeidsflyt
  Applikasjonen reagerer på hendelser med journalpost som har journalføringsoppgaver

  Bakgrunn: En journalføringsoppgave finnes i oppgave api
    Gitt nais applikasjon 'oppgave'
    Og en journalpostHendelse med journalpostId 2121212121 og fagområde 'BID'
    Og at det finnes en oppgave under behandling med oppgavetype 'JFR'

  Scenario: Arbeidsflyt skal ferdigstille oppgave med journalpostId ved hendelse
    Når hendelsen opprettes med aktør id '2771059330991' og journalstatus 'J'
    Og jeg søker etter oppgaver på fagområde 'BID' etter behandling av hendelse
    Så skal jeg ikke finne oppgave i søkeresultatet
