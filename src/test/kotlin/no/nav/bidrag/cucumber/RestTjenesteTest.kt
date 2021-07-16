package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.model.CucumberTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class RestTjenesteTest {

    @Test
    fun `gitt INGRESSES_FOR_TAGS med verdi for applikasjon, skal RestTjeneste konfigureres med denne verdien`() {
        val cucumberTests = CucumberTests(
            ingressesForTags = listOf("https://somewhere.com/@nais-app", "https://somewhere.else.com@annen-nais-app")
        )
        cucumberTests.initTestEnvironment()

        val restTjeneste = RestTjeneste("nais-app")
        val annenRestTjeneste = RestTjeneste("annen-nais-app")

        assertAll(
            { assertThat(restTjeneste.rest.baseUrl).`as`("tjeneste-app").isEqualTo("https://somewhere.com/nais-app/") },
            { assertThat(annenRestTjeneste.rest.baseUrl).`as`("annen-tjeneste").isEqualTo("https://somewhere.else.com/annen-nais-app/") }
        )
    }
}