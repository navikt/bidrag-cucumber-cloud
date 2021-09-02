package no.nav.bidrag.cucumber.sikkerhet

import no.nav.bidrag.cucumber.AZURE_LOGIN_ENDPOINT
import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.SECURITY_TOKEN
import no.nav.bidrag.cucumber.ScenarioManager
import org.slf4j.LoggerFactory
import org.springframework.web.client.RestTemplate

internal object Sikkerhet {
    @JvmStatic
    private val AZURE_AD_URL = "${AZURE_LOGIN_ENDPOINT}/${Environment.tenant}/oauth2/v2.0/token"

    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(Sikkerhet::class.java)

    private val tokenProvider = TokenProvider(RestTemplate())

    internal fun fetchAzureBearerToken(): String {
        try {
            return if (Environment.isNotSecurityTokenProvided()) {
                bearerTokenOf(tokenProvider.fetchAzureToken(AZURE_AD_URL))
            } else {
                bearerTokenOf(Environment.fetch(SECURITY_TOKEN)!!)
            }
        } catch (e: RuntimeException) {
            val exception = "${e.javaClass.name}: ${e.message} - ${e.stackTrace.first { it.fileName != null && it.fileName!!.endsWith("kt") }}"
            ScenarioManager.errorLog("Feil ved henting av online id token, $exception")

            if (Environment.isNotSanityCheck()) {
                throw e
            }

            return "na-sanity-check"
        }
    }

    private fun bearerTokenOf(token: String) = "Bearer $token"
}
