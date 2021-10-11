# language: no
@arbeidsflyt-opprett-oppgave
Egenskap: bidrag-arbeidsflyt: OPPRETT_OPPGAVE

  Tester nais applikasjon bidrag-arbeidsflyt
  Applikasjonen skal reagere på kafka hendelser og opprette oppgave for en ny journalpost

  Bakgrunn: En oppgave ikke finnes i oppgave api
    Gitt nais applikasjon 'oppgave'
    Og hendelse 'OPPRETT_OPPGAVE' for journalpostId 2020202020 og tema 'BID'
    Og at det ikke finnes en åpen oppgave

  Scenario: Arbeidsflyt skal opprette oppgave med journalpostId ved hendelse
    Når hendelsen opprettes
    Og jeg venter i et sekund slik at hendelse blir behandlet
    Og jeg søker etter oppgaven
    Så skal jeg finne oppgaven i søkeresultatet
