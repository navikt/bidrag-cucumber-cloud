package no.nav.bidrag.cucumber.cloud.sak

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.model.CucumberTestRun.Companion.hentRestTjeneste

@Suppress("unused") // brukes av cucumber
class SakEgenskaper : No {

    private lateinit var saksnummer: String

    init {
        Og("en sak med saksnr {string}") { saksnr: String ->
            saksnummer = saksnr
        }

        Når("jeg henter denne saken") {
            hentRestTjeneste().exchangeGet("/sak/$saksnummer")
        }
    }
}