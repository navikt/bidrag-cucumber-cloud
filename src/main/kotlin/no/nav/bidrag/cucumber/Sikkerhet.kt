package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.azure.Token
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

internal object Sikkerhet {

    private val LOGGER = LoggerFactory.getLogger(Sikkerhet::class.java)
    internal val SECURITY_FOR_APPLICATION: MutableMap<String, Security> = HashMap()

    internal fun fetchAzureToken(applicationName: String): String {
        try {
            return fetchToken(Environment.fetchIntegrationInput().fetchAzureInput(applicationName))
        } catch (e: RuntimeException) {
            val exception = "${e.javaClass.name}: ${e.message} - ${e.stackTrace.first { it.fileName != null && it.fileName!!.endsWith("kt") }}"
            LOGGER.error("Feil ved henting av online id token, $exception")
            throw e
        }
    }

    private fun fetchToken(azureInput: AzureInput): String {
        val integrationInput = Environment.fetchIntegrationInput()
        val applicationHostUrl = NaisConfiguration.CONFIG_FOR_APPLICATION[azureInput.name]?.applicationHostUrl
            ?: throw IllegalStateException("Fant ikke konfigurasjon for ${azureInput.name}")

        val azureAdUrl = "$applicationHostUrl/${azureInput.authorityEndpoint}/${azureInput.tenant}/oauth2/v2.0/token"
        val httpHeaders = HttpHeaders()
        val restTemplate = RestTemplate()

        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("client_id", azureInput.clientId)
        map.add("client_secret", azureInput.clientSecret)
        map.add("grant_type", "password")
        map.add("scope", "openid ${azureInput.clientId}/.default")
        map.add("username", integrationInput.fetchTenantUsername())
        map.add("password", integrationInput.userTestAuth)

        val request = HttpEntity(map, httpHeaders)
        val token = restTemplate.postForEntity(azureAdUrl, request, Token::class.java).body
            ?: throw IllegalStateException("Klarte ikke å hente token fra $azureAdUrl")

        LOGGER.info("Fetched id token for ${integrationInput.userTest}")

        return "Bearer ${token.token}"
    }
}

enum class Security {
    AZURE, NONE
}