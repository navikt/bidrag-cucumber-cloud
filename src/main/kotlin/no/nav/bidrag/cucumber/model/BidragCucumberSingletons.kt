package no.nav.bidrag.cucumber.model

import com.fasterxml.jackson.databind.ObjectMapper
import io.cucumber.java8.Scenario
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.cucumber.SpringConfig
import no.nav.bidrag.cucumber.hendelse.HendelseProducer
import org.springframework.context.ApplicationContext

/**
 * Singletons som er gyldige i en cucumber-kjøring og som er felles for ALLE egenskaper definert i feature-filer
 */
internal object BidragCucumberSingletons {
    @JvmStatic
    private val RUN_STATS = ThreadLocal<RunStats>()

    var hendelseProducer: HendelseProducer? = null
    var objectMapper: ObjectMapper? = null
    private var applicationContext: ApplicationContext? = null
    private var testMessagesHolder: TestMessagesHolder? = null

    fun hentPrototypeFraApplicationContext() = applicationContext?.getBean(HttpHeaderRestTemplate::class.java) ?: doManualInit()

    private fun doManualInit(): HttpHeaderRestTemplate {
        val httpComponentsClientHttpRequestFactory = SpringConfig().httpComponentsClientHttpRequestFactorySomIgnorererHttps()
        return HttpHeaderRestTemplate(httpComponentsClientHttpRequestFactory)
    }

    fun holdTestMessage(testMessage: String) {
        testMessagesHolder?.hold(testMessage)
    }

    fun addRunStats(scenario: Scenario) = fetchRunStats()
        .add(scenario)

    fun scenarioMessage(scenario: Scenario): String {
        val noScenario = scenario.name != null && scenario.name.isNotBlank()
        return if (noScenario) "'${scenario.name}'" else "scenario in ${scenario.uri}"
    }

    fun fetchTestMessagesWithRunStats() = fetchTestMessages() + "\n\n" + fetchRunStats()

    private fun fetchTestMessages() = testMessagesHolder?.fetchTestMessages() ?: "ingen loggmeldinger er produsert!"

    private fun fetchRunStats(): RunStats {
        var runStats = RUN_STATS.get()

        if (runStats == null) {
            runStats = RunStats()
            RUN_STATS.set(runStats)
        }

        return runStats
    }

    fun removeRunStats() {
        RUN_STATS.remove()
    }

    fun setApplicationContext(applicationContext: ApplicationContext) {
        BidragCucumberSingletons.applicationContext = applicationContext
    }

    fun setTestMessagesHolder(testMessagesHolder: TestMessagesHolder) {
        BidragCucumberSingletons.testMessagesHolder = testMessagesHolder
    }

    private class RunStats {
        val failedScenarios: MutableList<String> = ArrayList()
        private var passed = 0
        private var total = 0

        fun add(scenario: Scenario) {
            total = total.inc()

            if (scenario.isFailed) {
                val namelessScenario = scenario.name == null || scenario.name.isBlank()

                failedScenarios.add("${scenario.uri} # ${if (namelessScenario) "Nameless" else scenario.name}")
            } else {
                passed = passed.inc()
            }
        }

        fun get(): String {
            val noOfFailed = failedScenarios.size
            val failedScenariosString = if (failedScenarios.isEmpty()) "" else "Failed scenarios:\n${
                failedScenarios.joinToString(prefix = "- ", separator = "\n- ", postfix = "\n")
            }"

            return """
    Scenarios: $total
    Passed   : $passed
    Failed   : $noOfFailed
 
$failedScenariosString
"""
        }

        override fun toString(): String {
            return get()
        }
    }
}
