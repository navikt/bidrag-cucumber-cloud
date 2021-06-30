package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.BidragCucumberCloud.TEST_INGRESSES
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class RestTjenesteTest {

    @Test
    fun `gitt TEST_INGRESSES med verdi for applikasjon, skal RestTjeneste konfigureres med denne verdien`() {
        System.setProperty(TEST_INGRESSES, "nais-app@https://somewhere.com/,annen-nais-app@https://somewhere.else.com")
        val restTjeneste = RestTjeneste("nais-app")
        val annenRestTjeneste = RestTjeneste("annen-nais-app")

        assertAll(
            { assertThat(restTjeneste.rest.baseUrl).`as`("tjeneste-app").isEqualTo("https://somewhere.com/nais-app/") },
            { assertThat(annenRestTjeneste.rest.baseUrl).`as`("annen-tjeneste").isEqualTo("https://somewhere.else.com/annen-nais-app/") }
        )
    }
}