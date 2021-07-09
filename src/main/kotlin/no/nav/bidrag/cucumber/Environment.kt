package no.nav.bidrag.cucumber

import org.slf4j.LoggerFactory

internal object Environment {
    private val LOGGER = LoggerFactory.getLogger(Environment::class.java)

    val clientId: String get() = fetchPropertyOrEnvironment(AZURE_APP_CLIENT_ID) ?: ukjent(AZURE_APP_CLIENT_ID)
    val clientSecret: String get() = fetchPropertyOrEnvironment(AZURE_APP_CLIENT_SECRET) ?: ukjent(AZURE_APP_CLIENT_SECRET)
    val ingressesForTags: String get() = fetchPropertyOrEnvironment(INGRESSES_FOR_TAGS) ?: ukjent(INGRESSES_FOR_TAGS)
    val isSanityCheck: Boolean get() = fetchPropertyOrEnvironment(SANITY_CHECK)?.toBoolean() ?: false
    val userTest: String? get() = fetchPropertyOrEnvironment(TEST_USER)
    val userTestAuth: String get() = fetchPropertyOrEnvironment(TEST_AUTH) ?: ukjent(TEST_AUTH)
    val tenant: String get() = fetchPropertyOrEnvironment(AZURE_APP_TENANT_ID) ?: ukjent(AZURE_APP_TENANT_ID)
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
    private fun ukjent(property: String): String = throw IllegalStateException("Ingen $property å finne!")
    private fun fetchPropertyOrEnvironment(key: String): String? = System.getProperty(key) ?: System.getenv(key)
    fun isNotSanityCheck() = !isSanityCheck
}
