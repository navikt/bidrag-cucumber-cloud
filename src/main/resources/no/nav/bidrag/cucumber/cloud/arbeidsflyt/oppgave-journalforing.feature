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
    Når hendelsen opprettes med aktør id '123456789' og journalstatus 'J'
    Og jeg venter i et og et halvt sekund slik at hendelse blir behandlet
    Og jeg søker etter oppgaver på fagområde 'BID'
    Så skal jeg ikke finne oppgave i søkeresultatet
