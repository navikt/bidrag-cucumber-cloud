package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.model.CucumberTestRun
import no.nav.bidrag.cucumber.model.CucumberTestsModel
import org.slf4j.LoggerFactory

/**
 * Values gathered by environment variables or which is possible to override with system properties or environment variables
 */
internal object Environment {
    val withSecurityToken: Boolean get() = fetchPropertyOrEnvironment(SECURITY_TOKEN) != null

    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(Environment::class.java)

    private val testUsername: String? get() = fetchPropertyOrEnvironment(TEST_USER) ?: CucumberTestRun.testUsername

    val isSanityCheck: Boolean? get() = fetchPropertyOrEnvironment(SANITY_CHECK)?.toBoolean()
    val securityToken: String? get() = fetchPropertyOrEnvironment(SECURITY_TOKEN)
    val testUserAuth: String get() = fetchPropertyOrEnvironment(testAuthPropName()) ?: throw unknownState(testAuthPropName())

    fun fetchPropertyOrEnvironment(key: String): String? = System.getProperty(key) ?: System.getenv(key)

    private fun testAuthPropName() = TEST_AUTH + '_' + testUsername?.uppercase()

    private fun unknownState(name: String) = IllegalStateException("Ukjent miljøvariabel ($name), kjente: ${listKnownVariables()}!")

    private fun listKnownVariables() = ArrayList(System.getenv().keys).joinToString { it }

    fun initCucumberEnvironment(cucumberTestsModel: CucumberTestsModel) {
        LOGGER.info("Initializing environment for $cucumberTestsModel")
        CucumberTestRun(cucumberTestsModel).initEnvironment()
    }

    /**
     * removes properties and thread specific data values
     */
    fun reset() {
        System.clearProperty(SANITY_CHECK)
        System.clearProperty(SECURITY_TOKEN)
        System.clearProperty(TAGS)
        System.clearProperty(TEST_USER)
        CucumberTestRun.endRun()
    }

    fun asList(key: String): List<String> {
        return fetchPropertyOrEnvironment(key)?.split(",") ?: emptyList()
    }
}
