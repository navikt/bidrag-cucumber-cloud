package no.nav.bidrag.cucumber.cloud

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.BidragScenario
import org.assertj.core.api.Assertions.assertThat

class HelseEgenskap : No {
    init {
        Når("jeg kaller helsetjenesten") { BidragScenario.restTjeneste.exchangeGet("/actuator/health") }

        Og("header {string} skal være {string}") { navn: String, verdi: String ->
            val headere = BidragScenario.restTjeneste.hentHttpHeaders()

            assertThat(headere[navn]?.first()).isEqualTo(verdi)
        }
    }
}