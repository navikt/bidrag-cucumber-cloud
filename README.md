# bidrag-cucumber-cloud
Nais jobb som kjører integrasjonstester for nais mikrotjenester i bidrag som ligger i dev-gcp

## workflow
[![build and run naisjob](https://github.com/navikt/bidrag-cucumber-cloud/actions/workflows/build-and-run.yaml/badge.svg)](https://github.com/navikt/bidrag-cucumber-cloud/actions/workflows/build-and-run.yaml)
[![test build on pull request](https://github.com/navikt/bidrag-cucumber-cloud/actions/workflows/pr.yaml/badge.svg)](https://github.com/navikt/bidrag-cucumber-cloud/actions/workflows/pr.yaml)

## beskrivelse

Funksjonelle tester av bidrag nais applikasjoner som er designet for å kunne kjøres på en laptop som har innstallert naisdevice. Dette er vil først
og fremst gjelde applikasjoner som er deployet i google-cloud. Når applikasjonen ikke er satt opp med sikkerhet, kan den også nås herfra, sålenge
ingresser som kan brukes via et naisdevice brukes.

### Teknisk beskrivelse

Modulen er skrevet i Kotlin som gjør det enkelt å skape lett-leselige tester satt opp med `Gherkin`-filer (*.feature) som har norsk tekst og ligger i
`src/test/resources/<pakkenavn>`

BDD (Behaviour driven development) beskrives i `Gherkin`-filene (`*.featue`) som kjører automatiserte tester på bakgrunnen av funksjonaliteten som
skal støttes. Eks: på en `gherkin` fil på norsk 

```
01: # language: no
02  @oppslagstjenesten
03: Egenskap: oppslagstjeneste
04:   <detaljert beskrivelse av egenskapen>
05: 
06:   Scenario: fant ikke person
07:    Gitt liste over "tidligere ansatte"
09:    Når man forsøker å finne "Ola"
09:    Så skal svaret være "Ola" er ikke ansatt her
10:
11:  Scenario: fant person
12:    Gitt liste over "ansatte"
13:    Når man forsøker å finne "Per"
14:    Så skal svaret være "Per" er i "kantina"
```

Kort forklart:
- linje 1: språk
- linje 2: "tag" av test
- linje 3: egenskapen som testes (feature)
- linje 4: tekstlig beskrivelse av egenskapen
- linje 6: et scenario som denne egenskapen skal støtte
- linje 7: "Gitt" en ressurs
- linje 8: "Når" man utfører noe
- linje 9: "Så" forventer man et resultat
- linje 11: et annet scenario som denne egenskapen skal støtte
- linje 12-14: "Gitt" - "Når" - "Så": forventet oppførsel til dette scenarioet

Norske kodeord for `gherkin`: `Gitt` - `Når` - `Så` er de fremste kodeordene for norsk BDD.
Alle norske nøkkelord som kan brukes i `gherkin`-filer er `Egenskap`, `Bakgrunn`, `Scenario`, `Eksepmel`, `Abstrakt scenario`, `Gitt`, `Når`, `Og`,
`Men`, `Så`, `Eksempler`

Cucumber støtter flere språk og for mer detaljert oversikt over funksjonaliteten som `gherkin` gir, se detaljert beskrivelse på nett: 
<https://cucumber.io/docs/gherkin/reference/>

### Test av scenario

Et scenario for en nais applikasjon er implementert på følgende måte:
* en `*.feature` er tagget med applikasjonen som skal testes: `@<applikasjon>`, eks: `@bidrag-sak`, som vil være navnet på applikasjonen
  som er deployet.
* scenario-steget `Gitt nais applikasjon 'bidrag-sak'` vil hente ingressen som er oppgitt for nais applikasjonen som i dette tilfellet er `bidrag-sak`

### Miljøvariabler for kjøring

`TEST_USER` - Testbruker (saksbehandler) med ident ala z123456
`TEST_AUTH` - Passord til testbruker

#### Miljøvariabler for kjøring lokalt

`SANITY_CHECK=true` - for tjenester som har implementert sikkerhet, så må denne settes slik at selve sjekken bare logges til konsoll og ikke feiler...

#### Testbruker

Alle applikasjoner på gcp er i utgangspunktet sikret med Azure Ad. Et lag i denne sikkerhetet er kommunikasjon mellom applikasjoner. Derfor kan det
også være aktuelt å bruke en testbruker som simulerer en saksbehandler hos NAV. Dette er en såkalt Z-bruker og det må sørges for at den har et gyldig
passord og at "two factor authentication" er slått av for brukeren.

#### Kjøring lokalt

Tester i `bidrag-cucumber-cloud` er tilgjengelig fra et "naisdevice", men kjøring lokalt vil kun være en "sanity-check" for å sjekke at cucumber er
koblet opp riktig og at kjøring kan gjøres uten tekniske feil. Dette grunnet "zero trust" regimet som nais-applikasjonene kjører under. En applikasjon
som kjører uten sikkerhet kan testes lokalt.

Er det satt på sikkerhet, så kan ikke nais-applikasjonen testes fullt ut fra lokal kjøring. For å unngå at sjekker feiler når man kjører lokalt, så
må miljøvariabelen `SANITY_CHECK=true` settes. Den vil bare logge resultatet fra en kjøring til konsoll og ikke gjøre den aktuelle sjekken.

##### Kjøring med maven

Den simpleste formen er å bruke maven (Se avsnittet om miljøvariabler for kjøring for alle verdier som forventes):
```
mvn exec:java                                                                           \
    -D<en miljøvariabel="en verdi">                                                     \
    -D<en annen miljøvariabel="en annen verdi">                                         \
    -DSANITY_CHECK=true                                                                 \
    -DINTEGRATION_INPUT=sti til integationInput.json (se avsnittet 'Integration Input') \
    -Dexec.classpathScope=test                                                          \
    -Dexec.mainClass=no.nav.bidrag.cucumber.BidragCucumberCloud
```
##### Kjøring med IntelliJ

Man kan ogå bruke IntelliJ til å kjøre cucumber testene direkte. IntelliJ har innebygd støtte for cucumber (java), men hvis du vil navigeere i koden
ut fra testene som kjøres, så bør du installere plugin `Cucumber Kotlin` (IntelliJ settings/prefrences -> Plugins)

Kjør
* alle testene: høyreklikk på prosjektet og velg `Run 'All features in bidrag-cucumber-cloud'`
* en feature: høyreklikk på feature-fil, eks `sak.feature`prosjektet og velg `Run 'Feature: ...'`

Systemvariabler i maven-kommandoen (`-D<miljøvariabel=verdi`) må inn som miljøvariabler i IntelliJ. Se avsnittet "Miljøvariabler for kjøring"
for alle verdier som forventes.

* Dette gjøres i nedtrekksmenyen: `Select Run/Debug Configuration`.
  * Velg `Edit Configuration...` og legg inn miljøvariablene under `Environment variables:` i cucumber-testene som trenger dem
  * Når dette er gjort så kan du lagre denne konfigurasjonen ved å velge `Save '<feature(s)>' Configuration` fra nedtrekksmenyen.
  
For å fjerne feil om dublicate `*.feature`-filer i konfigurasjonen (fra valg om å kjøre alle features), så legg til `/src/test/resources` i feltet
`Feature or folder path:` fra `Edit configuration...`...
* dette girbare feil i konsoll fra Run-dialog og har ingen praktisk betydning 
