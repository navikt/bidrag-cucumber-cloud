package no.nav.bidrag.cucumber.azure

import io.cucumber.java8.No
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert

class BidragSakSteps() : No {

    companion object {
        private val configuration = Configuration()
        private val azureAdClient = AzureAdClient(configuration.azureAd)
        private val bidragSakClient = BidragSakClient(configuration.bidragSakBaseUrl, azureAdClient)

        fun henteSak(saksnr : String) : HttpResponse =
            runBlocking { bidragSakClient.henteSakMedGyldigToken(saksnr)}
    }

    private lateinit var saksnummer: String
    private lateinit var resultat: HttpResponse

    init {
        Gitt("en sak med saksnr {string}") { saksnr: String ->
            saksnummer = saksnr
        }

        Når("jeg henter denne saken") {
            resultat = henteSak(saksnummer)
        }

        Så("skal resultatet være {int}") { svar: Integer ->
            Assert.assertEquals(svar, resultat.status.value)
        }
    }
}