package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.model.CucumberTests
import org.slf4j.LoggerFactory

internal class Environment(cucumberTests: CucumberTests) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(Environment::class.java)
        private var cucumberTests: CucumberTests? = null
        private val ingressesForApps: MutableMap<String, String> = HashMap()

        val clientId: String get() = fetchPropertyOrEnvironment(AZURE_APP_CLIENT_ID) ?: unknownProperty(AZURE_APP_CLIENT_ID)
        val clientSecret: String get() = fetchPropertyOrEnvironment(AZURE_APP_CLIENT_SECRET) ?: unknownProperty(AZURE_APP_CLIENT_SECRET)
        val alleIngresserForApper: String get() = cucumberTests?.fetchIngressesForAppsAsString() ?: fetchNonNull(INGRESSES_FOR_APPS)
        val isSanityCheck: Boolean get() = cucumberTests?.sanityCheck ?: fetchPropertyOrEnvironment(SANITY_CHECK)?.toBoolean() ?: false
        val testUsername: String? get() = cucumberTests?.testUsername ?: fetchPropertyOrEnvironment(TEST_USER)
        val testUserAuth: String get() = fetchPropertyOrEnvironment(testAuthForTestUser()) ?: unknownProperty(testAuthForTestUser())
        val tenant: String get() = fetchPropertyOrEnvironment(AZURE_APP_TENANT_ID) ?: unknownProperty(AZURE_APP_TENANT_ID)
        val tenantUsername: String get() = "F_${testUsernameUppercase()}.E_${testUsernameUppercase()}@trygdeetaten.no"

        fun isNotSanityCheck() = !isSanityCheck
        fun isNotSecurityTokenProvided() = fetchPropertyOrEnvironment(SECURITY_TOKEN) == null
        fun isTestUserPresent() = testUsername != null
        fun fetch(propertyKey: String): String? = System.getProperty(propertyKey)

        private fun fetchNonNull(@Suppress("SameParameterValue") key: String) = fetchPropertyOrEnvironment(key) ?: unknownProperty(key)
        private fun fetchPropertyOrEnvironment(key: String) = fetch(key) ?: System.getenv(key)
        private fun testAuthForTestUser() = TEST_AUTH + '_' + testUsernameUppercase()
        private fun testUsernameUppercase() = testUsername?.uppercase()
        private fun unknownProperty(property: String): String = throw IllegalStateException("Ingen $property å finne!")

        fun fetchIngress(applicationName: String): String {
            if (ingressesForApps.isEmpty()) {
                readIngresses()
            }

            return ingressesForApps[applicationName] ?: throw IllegalStateException("Fant ikke ingress for $applicationName!")
        }

        private fun readIngresses() {
            alleIngresserForApper.split(',').forEach { string: String ->
                if (string.contains('@')) {
                    val (ingress, app) = splitIngressAndApplication(string)
                    ingressesForApps[app] = ingress
                } else {
                    LOGGER.error("kunne ikke lage ingress av $string")
                }
            }
        }

        private fun splitIngressAndApplication(string: String): Pair<String, String> {
            val ingress = string.split('@')[0]
            val ingressApp = string.split('@')[1]
            val app: String

            if (ingressApp.startsWith("tag:")) {
                app = ingressApp.substring(4)
            } else {
                app = ingressApp
            }

            LOGGER.info("Lager ingress av $string ($ingressApp vs $app)")

            return Pair(ingress, app)
        }

        fun resetTestEnvironment() {
            System.clearProperty(AZURE_APP_CLIENT_ID)
            System.clearProperty(AZURE_APP_CLIENT_SECRET)
            System.clearProperty(AZURE_APP_TENANT_ID)
            System.clearProperty(SANITY_CHECK)
            System.clearProperty(SECURITY_TOKEN)
            System.clearProperty(TEST_USER)
            System.clearProperty(TEST_USER + '_' + testUsernameUppercase())
            cucumberTests = null
            ingressesForApps.clear()
        }
    }

    init {
        Environment.cucumberTests = cucumberTests
    }
}
