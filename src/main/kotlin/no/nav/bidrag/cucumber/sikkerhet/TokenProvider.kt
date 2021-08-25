package no.nav.bidrag.cucumber.sikkerhet

import no.nav.bidrag.cucumber.Environment
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

class TokenProvider(private val provider: Provider) {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(TokenProvider::class.java)
    }

    constructor(restTemplate: RestTemplate) : this(DefaultProvider(restTemplate))

    internal fun fetchAzureToken(azureAdUrl: String): String {
        val httpHeaders = HttpHeaders()

        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("client_id", Environment.clientId)
        map.add("client_secret", Environment.clientSecret)
        map.add("grant_type", "password")
        map.add("scope", "openid ${Environment.clientId}/.default")
        map.add("username", Environment.tenantUsername)
        map.add("password", if (Environment.isNotSanityCheck()) Environment.testUserAuth else "sanity-check")

        LOGGER.info("> url    : $azureAdUrl")
        LOGGER.info("> headers: $httpHeaders")
        LOGGER.info("> map    : ${suppressPasswords(map)}")

        val request = HttpEntity(map, httpHeaders)
        val tokenJson = provider.postForEntity(azureAdUrl, request).body ?: throw IllegalStateException("Klarte ikke å hente token fra $azureAdUrl")

        LOGGER.info("Fetched id token for ${Environment.testUsername}")

        return tokenJson.token
    }

    private fun suppressPasswords(map: MultiValueMap<String, String>): String {
        val suppressed = HashMap<String, String?>()
        map.keys.forEach { key -> suppressed[key] = if (key.uppercase() != "PASSWORD") map.getValue(key).toString() else "[***]" }

        return suppressed.toString()
    }

    interface Provider {
        fun postForEntity(azureAdUrl: String, httpEntity: HttpEntity<*>): ResponseEntity<Token?>
    }

    class DefaultProvider(private val restTemplate: RestTemplate) : Provider {
        override fun postForEntity(azureAdUrl: String, httpEntity: HttpEntity<*>): ResponseEntity<Token?> {
            if (Environment.isNotSanityCheck()) {
                LOGGER.info("Hent azure token fra $azureAdUrl")
                return restTemplate.postForEntity(azureAdUrl, httpEntity, Token::class.java)
            }

            LOGGER.info("Henter ikke security taoken ved sanity check")
            return ResponseEntity.badRequest().build()
        }
    }
}
