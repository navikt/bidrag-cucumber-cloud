# bidrag-cucumber-cloud

Nais applikasjon som kjører integrasjonstester for applikasjoner som bruker Azure Ad og har ingress med tilgang via naisdevice

## workflow

[![build and deploy](https://github.com/navikt/bidrag-cucumber-cloud/actions/workflows/build-and-deploy.yaml/badge.svg)](https://github.com/navikt/bidrag-cucumber-cloud/actions/workflows/build-and-deploy.yaml)
[![test build on pull request](https://github.com/navikt/bidrag-cucumber-cloud/actions/workflows/pr.yaml/badge.svg)](https://github.com/navikt/bidrag-cucumber-cloud/actions/workflows/pr.yaml)
[![nightly run of cucumber tests](https://github.com/navikt/bidrag-cucumber-cloud/actions/workflows/run-nightly.yaml/badge.svg)](https://github.com/navikt/bidrag-cucumber-cloud/actions/workflows/run-nightly.yaml)

## beskrivelse

Funksjonelle tester av bidrag nais applikasjoner som er designet for å kunne kjøre en "sanity check" på en laptop som har innstallert naisdevice og
vil gjelde applikasjoner med azure i google cloud såfremt on-prem (når de har blitt eksponert for google cloud platform)

### Teknisk beskrivelse

Modulen er skrevet i Kotlin som gjør det enkelt å skape lett-leselige tester satt opp med `Gherkin`-filer (*.feature) som har norsk tekst og ligger i
`src/main/resources/<no.nav.bidrag.cucumber.cloud.pakkenavn>`

BDD (Behaviour driven development) beskrives i `Gherkin`-filene (`*.feature`) som kjører automatiserte tester på bakgrunnen av funksjonaliteten som
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

* en `*.feature` som er tagget, eks `@<tag>`, må ha en ingress for en nais applikasjon som brukes i feature
* scenario-steget `Gitt nais applikasjon 'bidrag-sak'` vil bruke ingressen som er oppgitt for nais applikasjonen som i dette tilfellet er `bidrag-sak`
* hvis en tag ikke er en nais applikasjon blant ingressen(e) som oppgies så må den nevnes i egen liste over tags som skal brukes

### Funksjonelle krav

Nedenfor så vises de nødvendige input for kjøring (1 og 2):

1. ingress som skal testes
2. tag som bruker denne ingressen
  * hvis ingressen til applikasjon også skal fungere som en tag, brukes `ingress@tag:<nais applikasjon>`
  * det finnes en egen liste for å liste opp tags, `tags`
  * **PS!** det er standard at appnavn blir brukt som context path etter ingress. Hvis det ikke skal gjøres må `noContextPathForApps` også listes opp

Dette er hva som må til for å kjøre testing av en applikasjon som ikke har sikkerhet. Når applikasjonen har sikkerhet implementert, må også en
testbruker angies.

3. testbruker (for simulering av nav-ident, bruker med sikkerhet implementert i Azure)

Hvis kjøring av denne applikasjonen gjøres lokalt (fra naisdevice) og mot en applikasjon som kjører under sikkerhet, så kan en fullstendig testkjøring
ikke gjøres uten at azure token blir sendt med når testen starter. Azure Ad brukes for å lage sikkerhetstoken for testbruker. Det er derfor nødvendig
med et ekstra parameter som forteller `bidrag-cucumber-cloud` at testbruker har et manuelt generert sikkerhetstoken eller kjøringen er en "sanity
check" for å teste at den tekniske implementasjonen til cucumber er ok.
* Vær obs på at en fullstendig kjøring fra et nais-device ikke kan gjøres (selv med manuelt token) hvis testen er designet slik at den kontakter
  applikasjonen direkte. GCP er satt opp med zero-trust og derfor vil applikasjonen ikke reagere på request fra en applikasjon (eller naisdevice) som
  ikke er definert med accessPolicy inbound i `nais.yaml`

4. securityToken
5. sanityCheck=true

Disse verdiene sendes som json til test-endepunkt, se avsnittet om `Kjøring lokalt`. Eksempel på en slik json (sanityCheck, noContextPathForApps, tags 
og testUser er valgfri):

```json
{
  "ingressesForApps": [
    "<ingress>@tag:app.a/tag.1>",
    "<ingress>@tag:app.b/tag.2>",
    "<ingress>@app.c>"
  ],
  "tags": ["<@tag.3>"],
  "noContextPathForApps": ["<app.b>"],
  "testUser": "z123456",
  "sanityCheck": true,
  "securityToken": "<azure token for testbruker>"
}
```

#### Azure Ad data for fullstendig kjøring

Lokalt på et naisdevice, vil sanity check av en cucumber test være alt som er mulig hvis ikke et sikkerhetstoken blir manuelt generert og sendt med
som input til testen. Applikasjonen som testes testes må også kunne nåes fra offentlig internett eller ha `accessPolicy` med "inbound rules" definert
i `nais.yaml`. Følgende azure data blir brukt til å hente sikkerhetstoken for test bruker når sikkerhetstoken ikke er del av json:

* `AZURE_APP_CLIENT_ID`: miljøvariabel fra kjørende nais applikasjon med azure
* `AZURE_APP_CLIENT_SECRET`: miljøvariabel fra kjørende nais applikasjon med azure
* `AZURE_APP_TENANT_ID`: miljøvariabel fra kjørende nais applikasjon med azure
* `AZURE_LOGIN_ENDPOINT`: login for testbruker sammen med azure verdier: https://login.microsoftonline.com/$AZURE_APP_TENANT_ID/oauth2/v2.0/token

### Variabler for kjøring

| json                   | Beskrivelse                                                                                                            | Kommentar                                                                    |
|------------------------|------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------|
| `ingressesForApps`     | kommaseparert liste over ingress og nais-applikasjon som testes                                                        | Eks: https://somewhere.com@nais.app.a,https://something.com@annen.nais.app.b |
| `noContextPathForApps` | kommaseparert liste over applikasjoner (fra `ingressesForApps`) som ikke bruker appnavn som context-path etter ingress |                                                                              |
| `tags`                 | kommaseparert liste over tags som skal kjøres (som ikke nevnes blant ingressene)                                       |                                                                              |
| `medSaksbehandlerType` | Typen på testbruker hvis autentisering skal gjøres i form av saksbehandler token                                       | BISYS_BASIS                                                                  |

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

```
mvn exec:java                                        \
    -DSANITY_CHECK=true                              \
    -DTEST_USER="<azure bruker ala z123456>          \
    -DSECURITY_TOKEN="<abc...xyz>                    \
    -DINGRESSES_FOR_APPS=<ingress@app1,ingress@app2> \
    -DTAGS=<@tag1>,<@tagp2>                          \
    -Dexec.mainClass=no.nav.bidrag.cucumber.BidragCucumberCloud
```

**NB**

* Fjern `-DSANITY_CHECK` (eller sett den til `-DSANITY_CHECH=false`) hvis du vil kjøre en fullskala test.
* Når `-DSANITY_CHECK=true` vil det være unødvendig å bruke `-DTEST_USER` og `-DSECURITY_TOKEN`.
* Når `-DSANITY_CHECK=false` (eller ikke er med) må man ha med `-DTEST_USER` og `-DSECURITY_TOKEN`.

###### Kjøring med maven og spring-boot

* åpne terminal og kjør kommandoen `mvn spring-boot:run`
* for sanity check, åpne ny terminal kjør kommandoen
  ```
  curl -X 'POST' http://localhost:8080/bidrag-cucumber-cloud/run \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '{"tags":["@tag1","@tag2"],"sanityCheck":true,"ingressesForApps":["<ingress.som.testes@tag:navn>"]}' \
       http://localhost:8080/bidrag-cucumber-cloud/run
  ```
* for fullstendig test, åpne ny terminal og kjør kommandoen
  ```
  curl -X 'POST' http://localhost:8080/bidrag-cucumber-cloud/run \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '{"tags":["@tag1","@tag2"],"testUsername":"<z123456>","ingressesForApps":["<ingress.som.testes@tag:navn>"],"securityToken"="<security token (uten Bearer)}'
  ```

##### Kjøring med IntelliJ

Man kan ogå bruke IntelliJ til å kjøre cucumber testene direkte. IntelliJ har innebygd støtte for cucumber (java), men hvis du vil navigere i koden
ut fra testene som kjøres, så bør du installere plugin `Cucumber Kotlin` (IntelliJ settings/prefrences -> Plugins)

###### Kjør cucumber features

* alle testene: høyreklikk på prosjektet og velg `Run 'All features in bidrag-cucumber-cloud'`
* en feature: høyreklikk på feature-fil, eks `sak.feature`prosjektet og velg `Run 'Feature: ...'`

**NB!**
Husk å legg inn miljøvariablene `SANITY_CHECK` og `INGRESSES_FOR_APPS` i `Edit Configurations...` under `Run`-drop down menyen...

###### Lagre features som er kjørt i intelliJ sin run-drop down

Det anbefales at man lagrer ovennevnte konfigurasjon, slik dette ikke må settes opp på ny...

* Velg `Save '<program/feature>' Configuration` fra `Run`-drop down menyen...

###### Kjør spring-boot i IntelliJ

* start spring-boot applikasjonen med IntelliJ
* for sanity check: åpne ny terminal kjør kommandoen
  ```
  curl -X 'POST' http://localhost:8080/bidrag-cucumber-cloud/run \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '{"tags":["@tag1","@tag2"],"sanityCheck":true,"ingressesForApps":["<ingress.som.testes@tag:navn>"]}'
  ```
* for fullstendig test, åpne ny terminal og kjør kommandoen
  ```
   curl -X 'POST' http://localhost:8080/bidrag-cucumber-cloud/run \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '{"tags":["@tag1","@tag2"],"testUsername":"<z123456>","ingressesForApps":["<ingress.som.testes@tag:navn>"],"securityToken"="<security token (uten Bearer)}'
  ```

##### Kjøring med swagger

###### lokalhost

1. Start spring-boot applikasjon
2. Gå til url: http://localhost:8080/bidrag-cucumber-cloud/swagger-ui/index.html?configUrl=/bidrag-cucumber-cloud/v3/api-docs/swagger-config#/
3. Logg inn med brukernavn/passord for basic auth. Du finner brukernavn/passord i filen [application-lokal-nais.yaml](src/test/resources/application-lokal-nais.yaml)
4. Ekspander endpoint `/run`
5. Trykk på "Try it out"
6. Endre json-schema med ingress@tag, sanity check evt. testbruker med security token
7. Press `Execute`

###### gcp

1. Gå til url for main eller feature branch
   * main - https://bidrag-cucumber-cloud.ekstern.dev.nav.no/bidrag-cucumber-cloud/swagger-ui/index.html?configUrl=/bidrag-cucumber-cloud/v3/api-docs/swagger-config#/
   * feature - https://bidrag-cucumber-cloud-feature.ekstern.dev.nav.no/bidrag-cucumber-cloud/swagger-ui/index.html?configUrl=/bidrag-cucumber-cloud/v3/api-docs/swagger-config#/
2. Logg inn med brukernavn/passord for basic auth. Du finner brukernavn/passord i kubernetes secrets `bidrag-cucumber-cloud-secrets`
3. Ekspander endpoint `/run`
4. Trykk på "Try it out"
5. Endre json-schema med ingress@tag, sanity check evt. testbruker med security token
6. Press `Execute`



#### Kjøre lokalt
For å kunne kjøre lokalt mot sky må du gjøre følgende

Åpne terminal på root mappen til `bidrag-cucumber-cloud`
Konfigurer kubectl til å gå mot kluster `dev-gcp`
```bash
# Log inn til GPC
gcp auth login --update-adc
# Sett cluster til dev-fss
kubectx dev-gcp
# Sett namespace til bidrag
kubens bidrag 

# -- Eller hvis du ikke har kubectx/kubens installert 
# (da må -n=bidrag legges til etter exec i neste kommando)
kubectl config use dev-gcp
```
Deretter kjør følgende kommando for å importere secrets. Viktig at filen som opprettes ikke committes til git

```bash
kubectl exec --tty deployment/bidrag-cucumber-cloud printenv | grep -E 'AZURE_|_URL|SCOPE|REST_AUTH|user_password' > src/main/resources/application-lokal-nais-secrets.properties
```

Start opp applikasjonen ved å kjøre [BidragCucumberCloudLokalNais.kt](src/test/kotlin/no/nav/bidrag/BidragCucumberCloudLokalNais.kt).
