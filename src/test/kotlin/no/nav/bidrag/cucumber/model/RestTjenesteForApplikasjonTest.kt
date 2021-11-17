package no.nav.bidrag.cucumber.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class RestTjenesteForApplikasjonTest {

    @Test
    fun `skal ikke bruke app navn som del av ingress, når applikasjon konfigureres og det spesifikt oppgies at denne ikke skal bli med`() {
        CucumberTestsModel(
            ingressesForApps = listOf("https://outside@this", "https://blow@that", "https://love@you"),
            noContextPathForApps = listOf("this", "that")
        ).initCucumberEnvironment()

        val thisUrl = RestTjenesteForApplikasjon.konfigurerApplikasjonUrlFor("this")
        val thatUrl = RestTjenesteForApplikasjon.konfigurerApplikasjonUrlFor("that")
        val youUel = RestTjenesteForApplikasjon.konfigurerApplikasjonUrlFor("you")

        assertAll(
            { assertThat(thisUrl).`as`("this url").isEqualTo("https://outside") },
            { assertThat(thatUrl).`as`("that url").isEqualTo("https://blow") },
            { assertThat(youUel).`as`("you url").isEqualTo("https://love/you") }
        )
    }
}