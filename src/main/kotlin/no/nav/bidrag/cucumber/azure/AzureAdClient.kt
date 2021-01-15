package no.nav.bidrag.cucumber.azure

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

class AzureAdClient(private val configuration: Configuration.AzureAd) {

    fun hentToken(): Token {
        val azureAdUrl = "${configuration.authorityEndpoint}/${configuration.tenant}/oauth2/v2.0/token"
        val restTemplate = RestTemplate()
        val httpHeaders = HttpHeaders()

        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("client_id", configuration.clientId)
        map.add("scope", "openid ${configuration.clientId}/.default")
        map.add("client_secret", configuration.clientSecret)
        map.add("username", configuration.username)
        map.add("password", configuration.password)
        map.add("grant_type", "password")

        val request = HttpEntity(map, httpHeaders)

        return restTemplate.postForEntity(azureAdUrl, request, Token::class.java).body
            ?: throw IllegalStateException("Klarte ikke å hente token fra $azureAdUrl")
    }
}