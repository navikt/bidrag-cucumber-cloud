# language: no
@arbeidsflyt-overfor-til-annen-enhet
Egenskap: en journalpost blir overført til en annen enhet

  Tester nais applikasjon bidrag-arbeidsflyt
  Applikasjonen reagerer på at en journalpost overføres til en annen enhet for behandling

  Bakgrunn: En oppgave lagret via oppgave api
    Gitt nais applikasjon 'oppgave'
    Og en journalpostHendelse med journalpostId 1234567 og fagområde 'BID'
    Og at det finnes en oppgave under behandling for enhet '4806'

  Scenario: Oppgave tilhører enhet
    Gitt at jeg søker etter oppgaven
    Så skal jeg finne oppgave i søkeresultatet med enhet '4806'

  Scenario: Overfør oppgave til annen enhet når hendelse behandles
    Gitt hendelsen opprettes med enhet '4812'
    Og jeg venter i et og et halvt sekund slik at hendelse blir behandlet
    Og jeg søker etter oppgaver på fagområde 'BID'
    Så skal jeg finne oppgave i søkeresultatet med enhet '4812'
