package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService
import no.nav.bidrag.cucumber.model.CucumberTestRun
import no.nav.bidrag.cucumber.model.CucumberTestsModel
import org.slf4j.LoggerFactory

internal object Environment {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(Environment::class.java)

    val isNotSanityCheck: Boolean get() = !isSanityCheck
    val isSanityCheck: Boolean get() = fetchPropertyOrEnvironment(SANITY_CHECK)?.toBoolean() ?: CucumberTestRun.isSanityCheck
    val isTestUserPresent: Boolean get() = testUsername != null

    val testUserAuth: String get() = fetchPropertyOrEnvironment(testAuthPropName()) ?: unknownProperty(testAuthPropName())
    val testUsername: String? get() = fetchPropertyOrEnvironment(TEST_USER) ?: CucumberTestRun.testUsername
    val tenantUsername: String get() = "F_${testUsernameUppercase()}.E_${testUsernameUppercase()}@trygdeetaten.no"

    private fun testAuthPropName() = TEST_AUTH + '_' + testUsernameUppercase()
    private fun testUsernameUppercase() = testUsername?.uppercase()
    private fun unknownProperty(property: String): String = throw IllegalStateException("Ingen $property å finne!")

    fun fetchIngress(applicationName: String): String = CucumberTestRun.fetchIngress(applicationName)

    fun initCucumberEnvironment(cucumberTestsModel: CucumberTestsModel) {
        LOGGER.info("Initializing environment for $cucumberTestsModel")
        CucumberTestRun(cucumberTestsModel).initEnvironment()
    }

    /**
     * removes thread specific data values
     */
    fun resetCucumberEnvironment() {
        System.clearProperty(SANITY_CHECK)
        System.clearProperty(SECURITY_TOKEN)
        System.clearProperty(TAGS)
        System.clearProperty(TEST_USER)
        CucumberTestRun.endRun()
        RestTjenesteForApplikasjon.removeAll()
        FellesEgenskaperService.fjernResttjenester()
    }

    fun fetchPropertyOrEnvironment(key: String): String? = System.getProperty(key) ?: System.getenv(key)
    fun isNoContextPathForApp(applicationName: String) =
        fromPropertyOrEnvironment(applicationName) ?: CucumberTestRun.isNoContextPathForApp(applicationName)

    fun asList(key: String): List<String> {
        return fetchPropertyOrEnvironment(key)?.split(",") ?: emptyList()
    }

    fun sleepInMillisecondsWhenWhenLive(milliseconds: Long) = if (isNotSanityCheck) Thread.sleep(milliseconds) else Unit
    private fun fromPropertyOrEnvironment(applicationName: String) = fetchPropertyOrEnvironment(NO_CONTEXT_PATH_FOR_APPS)?.contains(applicationName)
}
