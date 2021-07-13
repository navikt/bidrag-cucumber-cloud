# bidrag-cucumber-cloud

Nais applikasjon som kjører integrasjonstester for applikasjoner som bruker Azure Ad og har ingress med tilgang via naisdevice

## workflow

[![build and deploy](https://github.com/navikt/bidrag-cucumber-cloud/actions/workflows/build-and-deploy.yaml/badge.svg)](https://github.com/navikt/bidrag-cucumber-cloud/actions/workflows/build-and-deploy.yaml)
[![test build on pull request](https://github.com/navikt/bidrag-cucumber-cloud/actions/workflows/pr.yaml/badge.svg)](https://github.com/navikt/bidrag-cucumber-cloud/actions/workflows/pr.yaml)

## beskrivelse

Funksjonelle tester av bidrag nais applikasjoner som er designet for å kunne kjøres på en laptop som har innstallert naisdevice. Dette vil først og
fremst gjelde applikasjoner som er deployet i google-cloud. Når applikasjonen ikke er satt opp med sikkerhet, kan det også kjøres fullverdige tester
så lenge dette gjøres via et naisdevice.

### Teknisk beskrivelse

Modulen er skrevet i Kotlin som gjør det enkelt å skape lett-leselige tester satt opp med `Gherkin`-filer (*.feature) som har norsk tekst og ligger i
`src/main/resources/<no.nav.bidrag.cucumber.cloud.pakkenavn>`

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

Norske kodeord for `gherkin`: `Gitt` - `Når` - `Så` er de fremste kodeordene for norsk BDD. Alle norske nøkkelord som kan brukes i `gherkin`-filer
er `Egenskap`, `Bakgrunn`, `Scenario`, `Eksepmel`, `Abstrakt scenario`, `Gitt`, `Når`, `Og`, `Men`, `Så`, `Eksempler`

Cucumber støtter flere språk og for mer detaljert oversikt over funksjonaliteten som `gherkin` gir, se detaljert beskrivelse på nett:
<https://cucumber.io/docs/gherkin/reference/>

### Test av scenario

Et scenario for en nais applikasjon er implementert på følgende måte:

* en `*.feature` er tagget med applikasjonen som skal testes: `@<applikasjon>`, eks: `@bidrag-sak`. Tag'en vil bestemme hvilken ingress som brukes
  under testing
* scenario-steget `Gitt nais applikasjon 'bidrag-sak'` vil bruke ingressen som er oppgitt for nais applikasjonen som i dette tilfellet er `bidrag-sak`

### Funksjonelle krav

Nedenfor så vises de nødvendige input for kjøring (1 og 2):

1. ingress som skal testes
2. tag som bruker denne ingressen

Dette er hva som må til for å kjøre testing av en applikasjon som ikke har sikkerhet. Hvis applikasjonen har sikkerhet implementert, må også en
testbruker angies.

3. testbruker (for simulering av nav-ident, bruker med sikkerhet implementert i Azure)

Hvis kjøring av denne applikasjonen gjøres lokalt (fra naisdevice) og mot en applikasjon som kjører under sikkerhet, så kan en fullstendig testkjøring
ikke gjøres uten at azure token blir send med når testen starter. Azure Ad brukes for å lage sikkerhetstoken for testbruker. Det er derfor nødvendig
med et ekstra parameter som forteller `bidrag-cucumber-cloud` at testbruker har et manuelt generert sikkerhetstoken eller kjøringen er en "sanity
check" for å teste at den tekniske implementasjonen til cucumber er ok.

4. securityToken
5. sanityCheck=true

Disse verdiene sendes som json til test-endepunkt, se avsnittet om `Kjøring lokalt`. Eksempel på en slik json (sanityCheck og testUser er valgfri):

```json
{
  "ingressesForTags": [
    "<ingress>@app.a>",
    "<ingress>@app.b>",
    "<ingress>@app.c>"
  ],
  "testUser": "z123456",
  "sanityCheck": true,
  "securityToken": "<token for testbruker>"
}
```

#### Azure Ad data for fullstendig kjøring

Lokalt på et naisdevice, vil sanity check av en cucumber test være alt som er mulig hvis ikke et sikkerhetstoken blir manuelt generert og sendt med
som input til testen. Følgende azure data blir brukt til å hente sikkerhetstoken for test bruker:

* `AZURE_APP_CLIENT_ID`: miljøvariabel fra kjørende nais applikasjon med azure
* `AZURE_APP_CLIENT_SECRET`: miljøvariabel fra kjørende nais applikasjon med azure
* `AZURE_APP_TENANT_ID`: miljøvariabel fra kjørende nais applikasjon med azure
* `AZURE_LOGIN_ENDPOINT`: login for testbruker sammen med azure verdier: https://login.microsoftonline.com/$AZURE_APP_TENANT_ID/oauth2/v2.0/token

### Variabler for kjøring

json | Beskrivelse | Kommentar
---|---|---
`ingressesForTags` | kommaseparert liste over ingress og nais-applikasjon som testes| Eks: https://somewhere.com@nais.app.a,https://something.com@annen.nais.app.b
`testUser` | Testbruker (saksbehandler) med ident ala z123456 | unødvendig for sanity check, men må brukes med `securityToken` (hvis kjøring lokalt).

#### Miljøvariabler for kjøring lokalt

`SECURITY_TOKEN` - manuelt generert sikkerhetstoken for testbruker, Den kan også sendes med json til test-endpoint når testing foregår via spring-boot
`SANITY_CHECK=true` - for tjenester som har implementert sikkerhet, så kan denne settes slik at selve sjekken bare logges til konsoll og ikke feiler.
Den kan også sendes med json til test-endpoint når testing foregår via spring-boot

#### Testbruker

Testbruker simulerer en saksbehandler hos NAV. Testbrukeren må ha et gyldig brukernavn og passord som er sikret med Azure Ad. Dette er en såkalt
Z-bruker og det må sørges for at den har et gyldig passord og at "two factor authentication" er slått av for brukeren.

#### Kjøring lokalt

Tester i `bidrag-cucumber-cloud` er tilgjengelig fra et "naisdevice", men kjøring lokalt vil kun være en "sanity-check" for å sjekke at cucumber er
koblet opp riktig og at kjøring kan gjøres uten tekniske feil. Dette grunnet "zero trust" regimet som nais-applikasjonene på Azure AD kjører under. En
applikasjon som kjører uten sikkerhet kan dog testes lokalt.

Er det satt på sikkerhet, så kan ikke nais-applikasjonen testes fullt ut fra lokal kjøring. For å unngå at sjekker feiler når man kjører lokalt, så må
miljøvariabelen `SANITY_CHECK=true` settes. Da vil bare resultatene fra operasjoner som krever sikkerhet logges til konsoll i stedet for å den
aktuelle sjekken.

##### Kjøring med maven

Den simpleste formen er å bruke maven

```
mvn exec:java                                        \
    -DSANITY_CHECK=true                              \
    -DINGRESSES_FOR_TAGS=<ingress@tag1,ingress@tag2> \
    -Dexec.mainClass=no.nav.bidrag.cucumber.BidragCucumberCloud
```

**NB!**
Fjern `-DSANITY_CHECK` (eller sett den til `-DSANITY_CHECH=false`) hvis du vil kjøre en fullskala test av applikasjon uten sikkerhet.

###### Kjøring med maven og spring-boot

* åpne terminal og kjør kommandoen `mvn spring-boot:run`
* for sanity check, åpne ny terminal kjør kommandoen
  ```
  curl -H "Content-Type: application/json" \
       --request POST \
       --data '{"sanityCheck":true,"ingressesForTags":["<ingress.som.testes@tag>"]}' \
       http://localhost:8080/bidrag-cucumber-cloud/run
  ```
* for fullstendig test, åpne ny terminal og kjør kommandoen
  ```
  curl -H "Content-Type: application/json" \
       --request POST \
       --data '{"testUsername":"<z123456>","ingressesForTags":["<ingress.som.testes@tag>"],"securityToken"="<security token (uten Bearer)}' \
       http://localhost:8080/bidrag-cucumber-cloud/run
  ```

##### Kjøring med IntelliJ

Man kan ogå bruke IntelliJ til å kjøre cucumber testene direkte. IntelliJ har innebygd støtte for cucumber (java), men hvis du vil navigeere i koden
ut fra testene som kjøres, så bør du installere plugin `Cucumber Kotlin` (IntelliJ settings/prefrences -> Plugins)

###### Kjør cucumber features

* alle testene: høyreklikk på prosjektet og velg `Run 'All features in bidrag-cucumber-cloud'`
* en feature: høyreklikk på feature-fil, eks `sak.feature`prosjektet og velg `Run 'Feature: ...'`

**NB!**
Husk å legg inn miljøvariablene `SANITY_CHECK` og `INGRESSES_FOR_TAGS` i `Edit Configurations...` under `Run`-drop down menyen...

###### Lagre features som er kjørt

Det anbefales at man lagrer ovennevnte konfigurasjon, slik dette ikke må settes opp på ny...

* Velg `Save '<program/feature>' Configuration` fra `Run`-drop down menyen...

###### Kjør spring-boot i IntelliJ

* start spring-boot applikasjonen med IntelliJ
* for sanity check: åpne ny terminal kjør kommandoen
  ```
  curl -H "Content-Type: application/json" \
       --request POST \
       --data '{"sanityCheck":true,"ingressesForTags":["<ingress.som.testes@tag>"]}' \
       http://localhost:8080/bidrag-cucumber-cloud/run
  ```
* for fullstendig test, åpne ny terminal og kjør kommandoen
  ```
  curl -H "Content-Type: application/json" \
       --request POST \
       --data '{"testUsername":"<z123456>","ingressesForTags":["<ingress.som.testes@tag>"],"securityToken"="<security token (uten Bearer)}' \
       http://localhost:8080/bidrag-cucumber-cloud/run
  ```
