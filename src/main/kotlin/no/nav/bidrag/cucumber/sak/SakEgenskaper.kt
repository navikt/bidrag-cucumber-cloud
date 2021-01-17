package no.nav.bidrag.cucumber.sak

import io.cucumber.java8.No
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.runBlocking
import no.nav.bidrag.cucumber.BidragScenario
import no.nav.bidrag.cucumber.azure.AzureAdClient
import no.nav.bidrag.cucumber.azure.BidragSakClient
import no.nav.bidrag.cucumber.azure.Configuration
import org.assertj.core.api.Assertions.assertThat

class SakEgenskaper : No {
    companion object {
        private val configuration = Configuration()
        private val azureAdClient = AzureAdClient(configuration.azureAd)
        private val bidragSakClient = BidragSakClient(configuration.bidragSakBaseUrl, azureAdClient)

        fun henteSak(saksnr: String): HttpResponse =
            runBlocking { bidragSakClient.henteSakMedGyldigToken(saksnr) }
    }

    private lateinit var saksnummer: String
    private lateinit var resultat: HttpResponse

    init {
        Og("en sak med saksnr {string}") { saksnr: String ->
            saksnummer = saksnr
        }

        Når("jeg henter denne saken") {
            resultat = henteSak(saksnummer)
        }

        Når("jeg henter saken") {
            BidragScenario.restTjeneste.exchangeGet("sak/$saksnummer")
        }

        Så("skal resultatet være {int}") { svar: Int ->
            assertThat(resultat.status.value).isEqualTo(svar)
        }
    }
}