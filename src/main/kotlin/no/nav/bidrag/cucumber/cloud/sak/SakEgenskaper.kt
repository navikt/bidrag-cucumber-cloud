package no.nav.bidrag.cucumber.cloud.sak

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.BidragCucumberData

class SakEgenskaper : No {

    private lateinit var saksnummer: String

    init {
        Og("en sak med saksnr {string}") { saksnr: String ->
            saksnummer = saksnr
        }

        Når("jeg henter denne saken") {
            BidragCucumberData.restTjeneste.exchangeGet("sak/$saksnummer")
        }
    }
}