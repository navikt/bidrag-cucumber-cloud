package no.nav.bidrag.cucumber

import org.slf4j.LoggerFactory

internal object Environment {
    private val LOGGER = LoggerFactory.getLogger(Environment::class.java)

    val clientId: String get() = fetchPropertyOrEnvironment(AZURE_APP_CLIENT_ID) ?: ukjent(AZURE_APP_CLIENT_ID)
    val clientSecret: String get() = fetchPropertyOrEnvironment(AZURE_APP_CLIENT_SECRET) ?: ukjent(AZURE_APP_CLIENT_SECRET)
    val ingressesForTags: String get() = fetchPropertyOrEnvironment(INGRESSES_FOR_TAGS) ?: ukjent(INGRESSES_FOR_TAGS)
    val isSanityCheck: Boolean get() = fetchPropertyOrEnvironment(SANITY_CHECK)?.toBoolean() ?: false
    val testUsername: String? get() = fetchPropertyOrEnvironment(TEST_USER)
    val testUserAuth: String get() = fetchPropertyOrEnvironment(testAuthForTestUser()) ?: ukjent(testAuthForTestUser())
    val tenant: String get() = fetchPropertyOrEnvironment(AZURE_APP_TENANT_ID) ?: ukjent(AZURE_APP_TENANT_ID)
    val tenantUsername: String get() = "F_${testUsernameUppercase()}.E_${testUsernameUppercase()}@trygdeetaten.no"

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

    fun isNotSanityCheck() = !isSanityCheck
    fun isTestUserPresent() = testUsername != null
    internal fun testUsernameUppercase() = testUsername?.uppercase()
    private fun fetchPropertyOrEnvironment(key: String): String? = System.getProperty(key) ?: System.getenv(key)
    private fun testAuthForTestUser() = TEST_USER + "_" + testUsernameUppercase()
    private fun ukjent(property: String): String = throw IllegalStateException("Ingen $property å finne!")
}
