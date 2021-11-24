package no.nav.bidrag.cucumber.cloud.sak

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.model.CucumberTestRun.Companion.hentRestTjenesteTilTesting

@Suppress("unused") // brukes av cucumber
class SakEgenskaper : No {

    private lateinit var saksnummer: String

    init {
        Og("en sak med saksnr {string}") { saksnr: String ->
            saksnummer = saksnr
        }

        Når("jeg henter denne saken") {
            hentRestTjenesteTilTesting().exchangeGet("/sak/$saksnummer")
        }
    }
}