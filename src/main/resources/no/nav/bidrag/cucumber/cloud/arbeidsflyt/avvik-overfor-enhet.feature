# language: no
@arbeidsflyt-overfor-til-annen-enhet
Egenskap: bidrag-arbeidsflyt: AVVIK_OVERFOR_TIL_ANNEN_ENHET

  Tester nais applikasjon bidrag-arbeidsflyt
  Applikasjonen reagerer på kafka hendelse AVVIK_OVERFOR_TIL_ANNEN_ENHET og overfører eventuelle oppgaver til denne enheten

  Bakgrunn: En oppgave lagret via oppgave api
    Gitt nais applikasjon 'oppgave'
    Og hendelse 'AVVIK_OVERFOR_TIL_ANNEN_ENHET' for journalpostId 1234567 og tema 'BID'
    Og en oppgave for journalpostId 1234567 under tema 'BID' som tilhører enhet '4806'
    Og at det finnes en oppgave under behandling

  Scenario: Oppgave tilhører enhet
    Gitt at jeg søker etter oppgaven
    Så skal jeg finne oppgaven i søkeresultatet med enhet '4806'

  Scenario: Overfør oppgave til annen enhet når hendelse behandles
    Gitt hendelsen opprettes for overføring til enhet '4812'
    Og jeg venter i et sekund slik at hendelse blir behandlet
    Og jeg søker etter oppgaven
    Så skal jeg finne oppgaven i søkeresultatet med enhet '4812'
