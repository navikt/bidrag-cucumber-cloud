package no.nav.bidrag.cucumber.azure

import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.*

class AzureAdClient(private val configuration: Configuration.AzureAd) {

    suspend fun hentToken(): Token {
        val azureAdUrl = "${configuration.authorityEndpoint}/${configuration.tenant}/oauth2/v2.0/token"
        val formUrlEncode = listOf(
            "client_id" to configuration.clientId,
            "scope" to "openid ${configuration.clientId}/.default",
            "client_secret" to configuration.clientSecret,
            "username" to configuration.username,
            "password" to configuration.password,
            "grant_type" to "password"
        ).formUrlEncode()

        return apacheHttpClient.post {
            url(azureAdUrl)
            body = TextContent(formUrlEncode, ContentType.Application.FormUrlEncoded)
        }
    }
}