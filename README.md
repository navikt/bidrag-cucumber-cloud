# bidrag-cucumber-nais
Integrasjonstester for nais mikrotjenester i bidrag

## workflow
![build docker image](https://github.com/navikt/bidrag-cucumber-nais/workflows/build%20docker%20image/badge.svg)
![test on pull request](https://github.com/navikt/bidrag-cucumber-nais/workflows/test%20build%20on%20pull%20request/badge.svg)

## beskrivelse

Kotlin gjør det enkelt å skape lett leselig tester og dette er satt opp med `Gherkin`-filer (*.feature) som har norsk tekst og ligger i `src/test/resources/<pakkenavn>`

BDD (Behaviour driven development) beskrives i `Gherkin`-filene (`*.featue`) som kjører automatiserte tester på bakgrunnen av funksjonaliteten som skal støttes.
Eks: på en `gherkin` fil på norsk 

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
Alle norske nøkkelord som kan brukes i `gherkin`-filer er `Egenskap`, `Bakgrunn`, `Scenario`, `Eksepmel`, `Abstrakt scenario`, `Gitt`, `Når`, `Og`, `Men`, `Så`, `Eksempler`

Cucumber støtter flere språk og for mer detaljert oversikt over funksjonaliteten som `gherkin` gir, se detaljert beskrivelse på nett: 
<https://cucumber.io/docs/gherkin/reference/>

### GitHub Workflow

Arbeidsflyten har 2 deler. Del 1 vil bygge testkoden og kjøre vanlige enhetstester på denne koden. Del 2 vil utføre samtlige cucumber tester utført
med en standard jvm applikasjon som gjør en cucumber kjøring (se `IntegrationTests.kt`)

Når en push blir gjort til en branch eller main på en nais applikasjon under bidrag. Hvis en endring av en nais applikasjon krever endringer av
cucumber testene, så kod disse endringene i en feature branch tiljørende bidrag-cucumber-backend (feature branch har et annet navn enn `main`) og
denne branchen blir automatisk plukket opp av github workflow når cucumber-testene gjøres på en hvilken som helst feature-branch tilhørende nais-
applikasjonen.

### Kjøring

Alle kotlin-cucumber-tester kjører på en jvm og bruker JUnit som plattform. Derfor kan testene bli utført i hvilken som helt editor som støtter JUnit,
samt utføring fra bygg-verktøy som maven. Man kan også kjøre testene ved å kjøre applikasjonen implementert i `IntegrationTests.kt`.

Cucumber testene kan også kjøres fra kommandolinja med maven - `mvn test -P cucumber`

Uten at profilen `cucumber` oppgies, vil en vanlig kjøring med maven gjøres og enhetstester over testkoden vil kjøres.

Når `mvn test -P cucumber` kjøres blir alle `gherkin`-filene (*.feature) brukt til å kjøre tester. Det finnes også "tags" som brukes foran egenskaper
og scenario. Det er lagt opp til å "tagge" ei `gherkin`-fil med applikasjonsnavn slik at man kan angi å kjøre tester for en applikasjon alene. Det er
ikke noen begrensninger på hvor mange "tagger" en `Egenskap` eller `Feature` har, eller hvor mange filer som har den samme "taggen".

Kjøring av "taggede" tester:

```
mvn test -P cucumber -Dcucumber.filter.tags="@<tag name>"
```

For en mer detaljert oversikt over cucumber og api'ene som støttes: <https://cucumber.io/docs/cucumber/api/>  

Det er lagt opp til at testing kan gjøres med valgt applikasjon angitt. Følgende maven kommando blir da utført:

```
mvn test -P cucumber -Dcucumber.filter.tags=@<valgt-applikasjon> 
```

### Test rapportering
Etter at testing er gjennomført så kan man lage en rapport som blir tilgjengelig i `target/generated-report/index.html`. Dette gjøres av en maven-plugin:

```
mvn cluecumber-report:reporting
```
