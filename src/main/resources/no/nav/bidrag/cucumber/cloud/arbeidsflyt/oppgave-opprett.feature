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
    Når hendelsen opprettes med aktør id '2889800801806' og journalstatus 'M'
    Og jeg søker etter opprettet oppgave på fagområde 'BID', maks 3 ganger
    Så skal jeg finne oppgave i søkeresultatet med oppgavetypen 'JFR'

  Scenario: Arbeidsflyt skal ikke opprette oppgave med journalpostId ved hendelse
    Når hendelsen opprettes med aktør id '2889800801806' og journalstatus 'M'
    Og at det finnes en oppgave under behandling med oppgavetype 'JFR'
    Og jeg søker etter opprettet oppgave på fagområde 'BID', maks 3 ganger
    Så skal jeg finne totalt 1 oppgave(r) i søkeresultatet
