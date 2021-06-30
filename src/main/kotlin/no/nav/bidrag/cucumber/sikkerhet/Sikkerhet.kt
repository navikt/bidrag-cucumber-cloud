package no.nav.bidrag.cucumber.sikkerhet

import no.nav.bidrag.cucumber.Environment
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

internal object Sikkerhet {

    private val LOGGER = LoggerFactory.getLogger(Sikkerhet::class.java)

    internal fun fetchAzureBearerToken(): String {
        try {
            return "Bearer ${fetchAzureToken()}"
        } catch (e: RuntimeException) {
            val exception = "${e.javaClass.name}: ${e.message} - ${e.stackTrace.first { it.fileName != null && it.fileName!!.endsWith("kt") }}"
            LOGGER.error("Feil ved henting av online id token, $exception")
            throw e
        }
    }

    private fun fetchAzureToken(): String {
        val azureAdUrl = "${Environment.AZURE_LOGIN_ENDPOINT}/${Environment.tenant}/oauth2/v2.0/token"
        val httpHeaders = HttpHeaders()
        val restTemplate = RestTemplate()

        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("client_id", Environment.clientId)
        map.add("client_secret", Environment.clientSecret)
        map.add("grant_type", "password")
        map.add("scope", "openid ${Environment.clientId}/.default")
        map.add("username", Environment.tenantUsername)
        map.add("password", Environment.userTestAuth)

        LOGGER.info("> url    : $azureAdUrl")
        LOGGER.info("> headers: $httpHeaders")
        LOGGER.info("> map    : ${suppressPasswords(map)}")

        val request = HttpEntity(map, httpHeaders)
        val tokenJson = restTemplate.postForEntity(azureAdUrl, request, Token::class.java).body
            ?: throw IllegalStateException("Klarte ikke å hente token fra $azureAdUrl")

        LOGGER.info("Fetched id token for ${Environment.userTest}")

        return tokenJson.token
    }

    private fun suppressPasswords(map: MultiValueMap<String, String>): String {
        val suppressed = HashMap<String, String?>()
        map.keys.forEach { key -> suppressed[key] = if (key.uppercase() != "PASSWORD") map.getValue(key).toString() else "[***]" }

        return suppressed.toString()
    }
}
