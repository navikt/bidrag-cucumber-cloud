package no.nav.bidrag.cucumber.nais

import io.cucumber.java8.No
import org.assertj.core.api.Assertions.assertThat

class HelseEgenskap : No {
    init {
        Når("jeg kaller helsetjenesten") { FellesEgenskaper.restTjeneste.exchangeGet("/actuator/health") }

        Og("header {string} skal være {string}") { navn: String, verdi: String ->
            val headere = FellesEgenskaper.restTjeneste.hentHttpHeaders()

            assertThat(headere[navn]?.first()).isEqualTo(verdi)
        }

    }
}