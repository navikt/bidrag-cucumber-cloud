package no.nav.bidrag.cucumber.cloud

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.model.Assertion
import no.nav.bidrag.cucumber.model.BidragCucumberData

class HelseEgenskap : No {
    init {
        Når("jeg kaller helsetjenesten") { BidragCucumberData.restTjeneste.exchangeGet("/actuator/health") }

        Og("header {string} skal være {string}") { navn: String, verdi: String ->
            val headere = BidragCucumberData.restTjeneste.hentHttpHeaders()
            val headerVerdi = headere[navn]?.first()

            FellesEgenskaper.assertOrSanityCheck(
                Assertion("Header '$navn'", headerVerdi, verdi)
            )
        }
    }
}