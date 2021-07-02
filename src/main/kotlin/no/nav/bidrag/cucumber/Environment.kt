package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.BidragCucumberCloud.AZURE_APP_CLIENT_ID
import no.nav.bidrag.cucumber.BidragCucumberCloud.AZURE_APP_CLIENT_SECRET
import no.nav.bidrag.cucumber.BidragCucumberCloud.AZURE_APP_TENANT_ID
import no.nav.bidrag.cucumber.BidragCucumberCloud.INGRESSES_FOR_TAGS
import no.nav.bidrag.cucumber.BidragCucumberCloud.SANITY_CHECK
import no.nav.bidrag.cucumber.BidragCucumberCloud.TEST_AUTH
import no.nav.bidrag.cucumber.BidragCucumberCloud.TEST_USER
import org.slf4j.LoggerFactory

internal object Environment {
    const val AZURE_LOGIN_ENDPOINT = "https://login.microsoftonline.com"

    private val LOGGER = LoggerFactory.getLogger(Environment::class.java)

    val clientId: String get() = fetchPropertyOrEnvironment(AZURE_APP_CLIENT_ID) ?: throwException("Ingen $AZURE_APP_CLIENT_ID å finne")
    val clientSecret: String get() = fetchPropertyOrEnvironment(AZURE_APP_CLIENT_SECRET) ?: throwException("Ingen $AZURE_APP_CLIENT_SECRET å finne!")
    val isSanityCheck: Boolean get() = fetchPropertyOrEnvironment(SANITY_CHECK)?.toBoolean() ?: false
    val userTest: String? get() = fetchPropertyOrEnvironment(TEST_USER)
    val userTestAuth: String get() = fetchPropertyOrEnvironment(TEST_AUTH) ?: throwException("Ingen $TEST_AUTH å finne!")
    val tenant: String get() = fetchPropertyOrEnvironment(AZURE_APP_TENANT_ID) ?: throwException("Ingen $AZURE_APP_TENANT_ID å finne!")
    val tenantUsername: String get() = "F_${userTest?.uppercase()}.E_${userTest?.uppercase()}@trygdeetaten.no"

    fun fetchIngresses(): Map<String, String> {
        val ingresses: MutableMap<String, String> = HashMap()
        val ingressesString = fetchPropertyOrEnvironment(INGRESSES_FOR_TAGS) ?: throw IllegalStateException("Ingen '$INGRESSES_FOR_TAGS' å finne!")

        ingressesString.split(',').forEach { string: String ->
            if (string.contains('@')) {
                LOGGER.info("Lager ingress av $string")
                val ingress = string.split('@')[0]
                val app = string.split('@')[1]

                ingresses[app] = ingress
            } else {
                LOGGER.error("kunne ikke lage ingress av $string")
            }
        }

        return ingresses
    }

    fun isTestUserPresent() = userTest != null
    private fun throwException(message: String): String = throw IllegalStateException(message)
    private fun fetchPropertyOrEnvironment(key: String): String? = System.getProperty(key) ?: System.getenv(key)
    fun isNotSanityCheck() = !isSanityCheck
}
