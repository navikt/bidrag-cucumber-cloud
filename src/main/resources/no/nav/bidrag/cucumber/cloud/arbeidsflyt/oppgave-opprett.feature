# language: no
@arbeidsflyt-opprett-oppgave
Egenskap: en journalpost mottaksregistreres

  Tester nais applikasjon bidrag-arbeidsflyt
  Applikasjonen reagerer på at en journalpost mottaksregistreres og at det kanskje skal opprettes oppgave

  Bakgrunn: En oppgave ikke finnes i oppgave api
    Gitt nais applikasjon 'oppgave'
    Og en journalpostHendelse med journalpostId 2020202020 og fagområde 'BID'
    Og at det ikke finnes en åpen oppgave

  Scenario: Arbeidsflyt skal opprette oppgave med journalpostId ved hendelse
    Når hendelsen opprettes med aktør id '123456789' og journalstatus 'M'
    Og jeg venter i et sekund slik at hendelse blir behandlet
    Og jeg søker etter oppgaver på fagområde 'BID'
    Så skal jeg finne oppgave i søkeresultatet med oppgavetypen 'JFR'

  Scenario: Arbeidsflyt skal ikke opprette oppgave med journalpostId ved hendelse
    Når hendelsen opprettes uten aktør id, men med journalstatus 'M'
    Og jeg venter i et sekund slik at hendelse blir behandlet
    Og jeg søker etter oppgaver på fagområde 'BID'
    Så skal jeg ikke finne oppgave i søkeresultatet
