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

## Integration Input

Kjøring av [Cucumber](https://cucumber.io) testene krever at applikasjonen får data om integrasjonen som er påkrevd. Disse dataaene kan deles i to
grupper:
1) data som er felles for en test kjøring
   * `environment`: er det main eller feature branch som testes
   * `taggedTest`: (valgfritt) denne ene tag'en (eller samtlige når ingen verdi er oppgitt her)
   * `userTest`: testbrukeren som brukes for å kjøre testene
   * `naisProjectFolder`: en folder som inneholder nais konfigurasjon for applikasjonene som testes, `naisProjectFolder/<nais app som testes>/.nais/`
      * innholdet i `.nais` mappa skal være `nais.yaml` samt `main.yaml` eller `feature.yaml` 
2) data som er unike for applikasjonen som testes
   * `name`: navnet på applikasjonen
   * `clientId`: Azure client Id
   * `clientSecret`: Azure client secret
   * `tenant`: Azure tenant, eks for gcp-dev: `966ac572-f5b7-4bbe-aa88-c76419c0f851` (trygdeetaten.no)

Eksempel på ei slik integrasjonsfil kan sees under `src/test/resources/integrationInput.json` og det er forventet at denne fila oppgies i en 
miljøvariabel som heter `INTEGRATION_INPUT`

### Testbruker

Alle applikasjoner på gcp er i utgangspunktet sikret med Azure Ad. Dette er dog bare et lag av sikkerhet og sikrer bare kommunikasjon mellom
applikasjoner. Derfor vil det også være aktuelt å bruke en testbruker som simulerer en saksbehandler hos NAV. Dette er en såkalt Z-bruker og det må
sørges for at når den brukes ved testing, så må den ha et gyldig passord.
* test bruker hentes fra `IntegrationInput`: `userTest`
* passord settes i miljøvariabel: `TEST_AUTH`

### Kjøring lokalt

Tester i `bidrag-cucumber-cloud` er tilgjengelig fra et "naisdevice". Man må være koblet opp mot det nais-clusteret man tester mot.

#### Kjøring med maven

Den simpleste formen er å bruke maven:
```
mvn exec:java                                                                           \
    -DTEST_AUTH=passord til din testbruker (z1234567)                                   \
    -DINTEGRATION_INPUT=sti til integationInput.json (se avsnittet 'Integration Input') \
    -Dexec.classpathScope=test                                                          \
    -Dexec.mainClass=io.cucumber.core.cli.Main                                          \
    -Dexec.args="src/test/resources/no/nav/bidrag/cucumber/cloud --glue no.nav.bidrag.cucumber.cloud"
```
#### Kjøring med IntelliJ

Man kan ogå bruke IntelliJ til å kjøre cucumber testene direkte. IntelliJ har innebygd støtte for cucumber (java), men hvis du vil navigeere i koden
ut fra testene som kjøres, så bør du installere plugin `Cucumber Kotlin` (IntelliJ settings/prefrences -> Plugins)

Kjør
* alle testene: høyreklikk på prosjektet og velg `Run 'All features in bidrag-cucumber-cloud'`
* en feature: høyreklikk på feature-fil, eks `sak.feature`prosjektet og velg `Run 'Feature: ...'`

Programargumenter er i maven-kommandoen må inn som miljøvariabler for å kjøre testene i IntelliJ
```
  TEST_AUTH=passord til din testbruker (z1234567)
  INTEGRATION_INPUT=sti til integationInput.json (se avsnittet 'Integration Input')
```
* Dette gjøres i nedtrekksmenyen: `Select Run/Debug Configuration`.
  * Velg `Edit Configuration...` og legg inn miljøvariablene under `Environment variables:` i cucumber-testene som trenger dem
  * Når dette er gjort så kan du lagre denne konfigurasjonen ved å velge `Save '<feature(s)>' Configuration` fra nedtrekksmenyen.
