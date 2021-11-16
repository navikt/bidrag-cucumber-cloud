package no.nav.bidrag.cucumber.model

import io.cucumber.java8.Scenario
import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.INGRESSES_FOR_APPS
import no.nav.bidrag.cucumber.NO_CONTEXT_PATH_FOR_APPS
import no.nav.bidrag.cucumber.SANITY_CHECK
import no.nav.bidrag.cucumber.SECURITY_TOKEN
import no.nav.bidrag.cucumber.TAGS
import no.nav.bidrag.cucumber.dto.CucumberTestsApi

class CucumberTestRun(internal val cucumberTestsModel: CucumberTestsModel) {
    val tags: String get() = cucumberTestsModel.fetchTags()

    private val testMessagesHolder = TestMessagesHolder()

    private val runStats = RunStats()

    constructor(cucumberTestsApi: CucumberTestsApi) : this(CucumberTestsModel(cucumberTestsApi))

    fun initEnvironment(): CucumberTestRun {
        CUCUMBER_TEST_RUN.set(this)
        cucumberTestsModel.warningLogDifferences()

        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CucumberTestRun

        if (cucumberTestsModel != other.cucumberTestsModel) return false

        return true
    }

    override fun hashCode(): Int {
        return cucumberTestsModel.hashCode()
    }


    companion object {
        @JvmStatic
        private val CUCUMBER_TEST_RUN = ThreadLocal<CucumberTestRun>()

        @JvmStatic
        private fun thisRun(): CucumberTestRun {
            val cucumberTestRun = CUCUMBER_TEST_RUN.get()

            return cucumberTestRun ?: initFromEnvironment()
        }

        private fun initFromEnvironment(): CucumberTestRun {
            val cucumberTestRun = CucumberTestRun(
                CucumberTestsModel(
                    ingressesForApps = Environment.asList(INGRESSES_FOR_APPS),
                    noContextPathForApps = Environment.asList(NO_CONTEXT_PATH_FOR_APPS),
                    sanityCheck = Environment.fetchPropertyOrEnvironment(SANITY_CHECK)?.toBoolean(),
                    securityToken = Environment.fetchPropertyOrEnvironment(SECURITY_TOKEN),
                    tags = Environment.asList(TAGS)
                )
            )

            CUCUMBER_TEST_RUN.set(cucumberTestRun)

            return cucumberTestRun
        }

        val testUsername: String? get() = thisRun().cucumberTestsModel.testUsername
        val isSanityCheck: Boolean get() = thisRun().cucumberTestsModel.sanityCheck ?: false
        val isTestRunStarted: Boolean get() = CUCUMBER_TEST_RUN.get() != null

        fun addToRunStats(scenario: Scenario) = thisRun().runStats.add(scenario)
        fun fetchIngress(applicationName: String) = thisRun().cucumberTestsModel.fetchIngress(applicationName)
        fun fetchTestMessagesWithRunStats() = thisRun().testMessagesHolder.fetchTestMessages() + "\n\n" + thisRun().runStats.get()
        fun hold(logMessages: List<String>) = thisRun().testMessagesHolder.hold(logMessages)
        fun holdTestMessage(message: String) = thisRun().testMessagesHolder.hold(message)

        fun isNoContextPathForApp(applicationName: String) = thisRun().cucumberTestsModel.noContextPathForApps.contains(applicationName)

        fun holdExceptionForTest(throwable: Throwable) {
            val exceptionMessage = "${throwable.javaClass.simpleName}: ${throwable.message}"
            val cucumberTestRun = thisRun()

            cucumberTestRun.testMessagesHolder.hold(exceptionMessage)
            cucumberTestRun.runStats.addExceptionLogging(listOf(exceptionMessage))
        }

        fun endRun() {
            CUCUMBER_TEST_RUN.remove()
        }
    }
}
