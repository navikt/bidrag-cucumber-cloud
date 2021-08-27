package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.model.CucumberTestsDto
import org.slf4j.LoggerFactory

internal object Environment {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(Environment::class.java)

    @JvmStatic
    private val CUCUMBER_TESTS = ThreadLocal<CucumberTestsDto>()

    @JvmStatic
    private val INGRESS_FOR_APP = ThreadLocal<MutableMap<String, String>>()

    internal val alleIngresserForApper: String
        get() = fetchPropertyOrEnvironment(INGRESSES_FOR_APPS) ?: CUCUMBER_TESTS.get()?.fetchIngressesForAppsAsString() ?: ""

    val clientId: String get() = fetchPropertyOrEnvironment(AZURE_APP_CLIENT_ID) ?: "unknown-AZURE_APP_CLIENT_ID"
    val clientSecret: String get() = fetchPropertyOrEnvironment(AZURE_APP_CLIENT_SECRET) ?: "unknown-AZURE_APP_CLIENT_SECRET"
    val isSanityCheck: Boolean get() = fetchPropertyOrEnvironment(SANITY_CHECK)?.toBoolean() ?: CUCUMBER_TESTS.get()?.sanityCheck ?: false
    val testUsername: String? get() = fetchPropertyOrEnvironment(TEST_USER) ?: CUCUMBER_TESTS.get()?.testUsername
    val testUserAuth: String get() = fetchPropertyOrEnvironment(testAuthForTestUser()) ?: unknownProperty(testAuthForTestUser())
    val tenant: String get() = fetchPropertyOrEnvironment(AZURE_APP_TENANT_ID) ?: "unknown-AZURE_APP_TENANT_ID"
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
        if (needToFetchIngresses()) {
            fetchIngresses()
        }

        return INGRESS_FOR_APP.get()[applicationName] ?: throw IllegalStateException("Fant ikke ingress for $applicationName!")
    }

    private fun needToFetchIngresses(): Boolean {
        if (INGRESS_FOR_APP.get() == null) {
            INGRESS_FOR_APP.set(HashMap())
            return true
        }

        return INGRESS_FOR_APP.get().isEmpty()
    }

    private fun fetchIngresses() {
        alleIngresserForApper.split(',').forEach { string: String ->
            if (string.contains('@')) {
                val (ingress, app) = splitIngressAndApplication(string)
                INGRESS_FOR_APP.get()[app] = ingress
            } else {
                LOGGER.error("kunne ikke lage ingress av $string")
            }
        }
    }

    private fun splitIngressAndApplication(string: String): Pair<String, String> {
        val ingress = string.split('@')[0]
        val ingressApp = string.split('@')[1]

        val app = if (ingressApp.startsWith("tag:")) {
            ingressApp.substring(4)
        } else {
            ingressApp
        }

        LOGGER.info("Ingress@naisApp: $string")

        return Pair(ingress, app)
    }

    fun initCucumberEnvironment(cucumberTestsDto: CucumberTestsDto) {
        LOGGER.info("Initializing environment for $cucumberTestsDto")
        CUCUMBER_TESTS.set(cucumberTestsDto)
        cucumberTestsDto.warningLogDifferences()
    }

    fun resetCucumberEnvironment() {
        System.clearProperty(SANITY_CHECK)
        System.clearProperty(SECURITY_TOKEN)
        System.clearProperty(TEST_USER)
        CUCUMBER_TESTS.remove()
        INGRESS_FOR_APP.remove()
    }

    fun isNotontextPathForApp(applicationName: String): Boolean {
        val noContextPath = if (fetchPropertyOrEnvironment(NO_CONTEXT_PATH_FOR_APPS) == null) {
            CUCUMBER_TESTS.get()?.noContextPathForApps?.contains(applicationName)
        } else {
            fetchPropertyOrEnvironment(NO_CONTEXT_PATH_FOR_APPS).contains(applicationName)
        }

        return noContextPath ?: false
    }
}
