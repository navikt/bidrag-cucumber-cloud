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

    val clientId: String get() = fetchPropertyOrEnvironment(AZURE_APP_CLIENT_ID) ?: throwException(AZURE_APP_CLIENT_ID)
    val clientSecret: String get() = fetchPropertyOrEnvironment(AZURE_APP_CLIENT_SECRET) ?: throwException(AZURE_APP_CLIENT_SECRET)
    val ingressesForTags: String get() = fetchPropertyOrEnvironment(INGRESSES_FOR_TAGS) ?: throwException(INGRESSES_FOR_TAGS)
    val isSanityCheck: Boolean get() = fetchPropertyOrEnvironment(SANITY_CHECK)?.toBoolean() ?: false
    val userTest: String? get() = fetchPropertyOrEnvironment(TEST_USER)
    val userTestAuth: String get() = fetchPropertyOrEnvironment(TEST_AUTH) ?: throwException(TEST_AUTH)
    val tenant: String get() = fetchPropertyOrEnvironment(AZURE_APP_TENANT_ID) ?: throwException(AZURE_APP_TENANT_ID)
    val tenantUsername: String get() = "F_${userTest?.uppercase()}.E_${userTest?.uppercase()}@trygdeetaten.no"

    fun fetchIngresses(): Map<String, String> {
        val ingresses: MutableMap<String, String> = HashMap()

        ingressesForTags.split(',').forEach { string: String ->
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
    private fun throwException(property: String): String = throw IllegalStateException("Ingen $property å finne!")
    private fun fetchPropertyOrEnvironment(key: String): String? = System.getProperty(key) ?: System.getenv(key)
    fun isNotSanityCheck() = !isSanityCheck
}
