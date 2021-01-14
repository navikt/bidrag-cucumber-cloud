package no.nav.bidrag.cucumber.azure

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import org.slf4j.LoggerFactory

class BidragSakClient(
    private val baseUrl: String,
    private val azureAdClient: AzureAdClient
) {

    private val LOGGER = LoggerFactory.getLogger(BidragSakClient::class.java)

    suspend fun henteSakMedGyldigToken(saksnr: String): HttpResponse {
        val token = azureAdClient.hentToken();
        return henteSak(saksnr, token.token)
    }


    private suspend fun henteSak(saksnr: String, token: String): HttpResponse {
        LOGGER.info("Ber bidrag-sak om å hente sak med saksnr: $saksnr")

        return httpClient.get {
            url("$baseUrl/sak/$saksnr")
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
        }
    }
}


