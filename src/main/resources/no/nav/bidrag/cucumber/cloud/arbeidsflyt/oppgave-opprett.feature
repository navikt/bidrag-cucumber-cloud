# language: no
@arbeidsflyt-opprett-oppgave
Egenskap: en journalpost mottaksregistreres

  Tester nais applikasjon bidrag-arbeidsflyt
  Applikasjonen reagerer på at en journalpost mottaksregistreres og at det kanskje skal opprettes oppgave

  Bakgrunn: En oppgave ikke finnes i oppgave api
    Gitt nais applikasjon 'oppgave'
    Og en journalpostHendelse med journalpostId 2020202020 og fagområde 'BID'
    Og at det ikke finnes en åpen oppgave

  Scenario: Arbeidsflyt skal opprette oppgave med journalpostId og aktorid ved hendelse
    Når hendelsen opprettes med aktør id '2771059330991' og journalstatus 'M'
    Og jeg søker etter opprettet oppgave på fagområde 'BID', maks 3 ganger
    Så skal jeg finne oppgave i søkeresultatet med oppgavetypen 'JFR'
    Så skal jeg finne oppgave i søkeresultatet med aktorId '2771059330991'

  Scenario: Arbeidsflyt skal opprette oppgave med journalpostId og fnr ved hendelse
    Når hendelsen opprettes med fnr '02439340331' og journalstatus 'M'
    Og jeg søker etter opprettet oppgave på fagområde 'BID', maks 3 ganger
    Så skal jeg finne oppgave i søkeresultatet med oppgavetypen 'JFR'
    Så skal jeg finne oppgave i søkeresultatet med aktorId '2771059330991'

  Scenario: Arbeidsflyt skal ikke opprette oppgave med journalpostId ved hendelse når journalføringsoppgave finnes fra før
    Gitt at det finnes en oppgave under behandling med oppgavetype 'JFR'
    Når hendelsen opprettes med aktør id '2771059330991' og journalstatus 'M'
    Og jeg søker etter opprettet oppgave på fagområde 'BID', maks 3 ganger
    Så skal jeg finne totalt 1 oppgaver i søkeresultatet
