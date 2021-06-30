package no.nav.bidrag.cucumber.cloud

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.BidragScenario

class HelseEgenskap : No {
    init {
        Når("jeg kaller helsetjenesten") { BidragScenario.restTjeneste.exchangeGet("/actuator/health") }

        Og("header {string} skal være {string}") { navn: String, verdi: String ->
            val headere = BidragScenario.restTjeneste.hentHttpHeaders()
            val headerVerdi = headere[navn]?.first()

            FellesEgenskaper.sanityCheck(
                Assertion("Header '$navn'", headerVerdi, verdi)
            )
        }
    }
}