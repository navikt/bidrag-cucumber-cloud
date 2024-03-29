# language: no
@arbeidsflyt-overfor-til-annen-enhet
Egenskap: en journalpost blir overført til en annen enhet

  Tester nais applikasjon bidrag-arbeidsflyt
  Applikasjonen reagerer på at en journalpost overføres til en annen enhet for behandling

  Bakgrunn: En oppgave lagret via oppgave api
    Gitt nais applikasjon 'oppgave'
    Og en journalpostHendelse med journalpostId 1234567 og fagområde 'BID'
    Og at det finnes en oppgave under behandling for enhet '4812'

  @ignored # av en eller annen grunn feiler negativ test uten exception?
  Scenario: Oppgave tilhører enhet
    Gitt at jeg søker etter oppgaven etter behandling av hendelse
    Så skal jeg finne oppgave i søkeresultatet med enhet '4812'

  Scenario: Overfør oppgave til annen enhet når hendelse behandles
    Gitt hendelsen opprettes med enhet '4833'
    Og jeg venter to sekunder slik at hendelsen kan bli behandlet
    Og jeg søker etter opprettet oppgave på fagområde 'BID'
    Så skal jeg finne oppgave i søkeresultatet med enhet '4833'
