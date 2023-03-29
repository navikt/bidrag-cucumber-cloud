package no.nav.bidrag.cucumber.model

import io.cucumber.java8.Scenario
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.Environment.fetchPropertyOrEnvironment
import no.nav.bidrag.cucumber.INGRESSES_FOR_APPS
import no.nav.bidrag.cucumber.NO_CONTEXT_PATH_FOR_APPS
import no.nav.bidrag.cucumber.SANITY_CHECK
import no.nav.bidrag.cucumber.SECURITY_TOKEN
import no.nav.bidrag.cucumber.ScenarioManager
import no.nav.bidrag.cucumber.TAGS
import no.nav.bidrag.cucumber.TEST_USER
import no.nav.bidrag.cucumber.cloud.FellesEgenskaper
import no.nav.bidrag.cucumber.dto.CucumberTestsApi
import no.nav.bidrag.cucumber.dto.SaksbehandlerType

class CucumberTestRun(private val cucumberTestsModel: CucumberTestsModel) {
    internal val testData = TestData()
    private val restTjenester = RestTjenester()
    private val runStats = RunStats()
    private val testMessagesHolder = TestMessagesHolder()

    val tags: String get() = cucumberTestsModel.fetchTags()

    constructor(cucumberTestsApi: CucumberTestsApi) : this(CucumberTestsModel(cucumberTestsApi))

    fun initEnvironment(): CucumberTestRun {
        CUCUMBER_TEST_RUN.set(this)

        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CucumberTestRun

        return cucumberTestsModel == other.cucumberTestsModel
    }

    override fun hashCode(): Int {
        return cucumberTestsModel.hashCode()
    }

    companion object {

        @JvmStatic
        private val CUCUMBER_TEST_RUN = ThreadLocal<CucumberTestRun>()

        @JvmStatic
        fun thisRun() = CUCUMBER_TEST_RUN.get() ?: initFromEnvironment()

        @JvmStatic
        private fun initFromEnvironment(): CucumberTestRun {
            val cucumberTestRun = CucumberTestRun(
                CucumberTestsModel(
                    ingressesForApps = Environment.asList(INGRESSES_FOR_APPS),
                    noContextPathForApps = Environment.asList(NO_CONTEXT_PATH_FOR_APPS),
                    sanityCheck = fetchPropertyOrEnvironment(SANITY_CHECK)?.toBoolean(),
                    securityToken = fetchPropertyOrEnvironment(SECURITY_TOKEN),
                    tags = Environment.asList(TAGS)
                )
            )

            CUCUMBER_TEST_RUN.set(cucumberTestRun)

            return cucumberTestRun
        }

        val isNotSanityCheck: Boolean get() = !isSanityCheck
        val isSanityCheck: Boolean get() = Environment.isSanityCheck ?: thisRun().cucumberTestsModel.sanityCheck ?: false
        val isTestRunStarted: Boolean get() = CUCUMBER_TEST_RUN.get() != null
        val isTestUserPresent: Boolean get() = fetchPropertyOrEnvironment(TEST_USER) != null || thisRun().cucumberTestsModel.testUsername != null
        val securityToken: String? get() = thisRun().cucumberTestsModel.securityToken
        val testUsername: String? get() = thisRun().cucumberTestsModel.testUsername
        val skipAuth: Boolean get() = thisRun().cucumberTestsModel.skipAuth
        val saksbehandlerType: SaksbehandlerType? get() = thisRun().cucumberTestsModel.saksbehandlerType
        val withSecurityToken: Boolean get() = securityToken != null
        private var exceptionLogger: ExceptionLogger? = null

        fun addToRunStats(scenario: Scenario) = thisRun().runStats.add(scenario)
        fun fetchIngress(applicationName: String) = thisRun().cucumberTestsModel.fetchIngress(applicationName)
        fun fetchTestMessagesWithRunStats() = thisRun().testMessagesHolder.fetchTestMessages() + "\n\n" + thisRun().runStats.get()
        fun hentRestTjenste(applicationName: String) = thisRun().restTjenester.hentRestTjeneste(applicationName)
        fun hentRestTjenesteTilTesting() = thisRun().restTjenester.hentRestTjenesteTilTesting()
        fun hold(logMessages: List<String>) = thisRun().testMessagesHolder.hold(logMessages)
        fun holdTestMessage(message: String) = thisRun().testMessagesHolder.hold(message)
        fun isApplicationConfigured(applicationName: String) = thisRun().restTjenester.isApplicationConfigured(applicationName)
        fun isNoContextPathForApp(applicationName: String) = thisRun().cucumberTestsModel.noContextPathForApps.contains(applicationName)
        fun settOppNaisApp(naisApplikasjon: String) = thisRun().restTjenester.settOppNaisApp(naisApplikasjon)
        fun settOppNaisAppTilTesting(naisApplikasjon: String) = thisRun().restTjenester.settOppNaisAppTilTesting(naisApplikasjon)
        fun sleepWhenNotSanityCheck(milliseconds: Long) = if (isNotSanityCheck) Thread.sleep(milliseconds) else Unit
        fun updateSecurityToken(securityToken: String?) = thisRun().cucumberTestsModel.updateSecurityToken(securityToken)

        fun holdExceptionForTest(throwable: Throwable) {
            if (exceptionLogger == null) {
                exceptionLogger = BidragCucumberSingletons.hentEllerInit(ExceptionLogger::class)
            }

            val exceptionLog = exceptionLogger!!.logException(
                throwable, throwable.stackTrace.first {
                    (it.className.contains("no.nav") || it?.fileName?.contains("feature") ?: false)
                            && !it.className.contains(CucumberTestRun::class.simpleName!!)
                            && !it.className.contains(FellesEgenskaper::class.simpleName!!)
                            && !it.className.contains(ScenarioManager::class.simpleName!!)
                }.className
            )

            val exceptionMessage = exceptionLog[0]
            val cucumberTestRun = thisRun()

            cucumberTestRun.testMessagesHolder.hold(exceptionMessage)
            cucumberTestRun.runStats.addExceptionLogging(exceptionLog)
        }

        fun endRun() {
            CUCUMBER_TEST_RUN.remove()
        }
    }
}
