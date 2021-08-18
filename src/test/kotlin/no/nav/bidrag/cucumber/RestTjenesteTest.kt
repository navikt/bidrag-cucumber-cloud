package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.model.CucumberTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class RestTjenesteTest {

    @Test
    fun `gitt INGRESSES_FOR_APPS med verdi for applikasjon, skal RestTjeneste konfigureres med denne verdien`() {
        val cucumberTests = CucumberTests(
            ingressesForApps = listOf("https://somewhere.com/@nais-app", "https://somewhere.else.com@annen-nais-app")
        )

        cucumberTests.initCucumberEnvironment()

        val restTjeneste = RestTjeneste("nais-app")
        val annenRestTjeneste = RestTjeneste("annen-nais-app")

        assertAll(
            { assertThat(restTjeneste.rest.baseUrl).`as`("tjeneste-app").isEqualTo("https://somewhere.com/nais-app/") },
            { assertThat(annenRestTjeneste.rest.baseUrl).`as`("annen-tjeneste").isEqualTo("https://somewhere.else.com/annen-nais-app/") }
        )
    }

    @Test
    fun `gitt INGRESSES_FOR_APPS med verdi for applikasjon (konfigurert som er en tag), skal RestTjeneste konfigureres med tag-navnet`() {
        val cucumberTests = CucumberTests(
            ingressesForApps = listOf("https://somewhere.com/@tag:nais-tag", "https://somewhere.else.com@tag:annen-nais-tag")
        )

        cucumberTests.initCucumberEnvironment()

        val restTjeneste = RestTjeneste("nais-tag")
        val annenRestTjeneste = RestTjeneste("annen-nais-tag")

        assertAll(
            { assertThat(restTjeneste.rest.baseUrl).`as`("tjeneste-app").isEqualTo("https://somewhere.com/nais-tag/") },
            { assertThat(annenRestTjeneste.rest.baseUrl).`as`("annen-tjeneste").isEqualTo("https://somewhere.else.com/annen-nais-tag/") }
        )
    }
}