package no.nav.bidrag.cucumber.model

import com.fasterxml.jackson.databind.ObjectMapper
import io.cucumber.java8.Scenario
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.cucumber.SpringConfig
import no.nav.bidrag.cucumber.hendelse.HendelseProducer
import no.nav.bidrag.cucumber.sikkerhet.SecurityTokenService
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
    private var exceptionLogger: ExceptionLogger? = null
    private var testMessagesHolder: TestMessagesHolder? = null

    fun hentPrototypeFraApplicationContext() = applicationContext?.getBean(HttpHeaderRestTemplate::class.java) ?: doManualInit()
    fun hentTokenServiceFraContext() = applicationContext?.getBean(SecurityTokenService::class.java)

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

    fun holdExceptionForTest(exception: Exception) {
        val messages = exceptionLogger?.logException(exception, BidragCucumberSingletons::class.java.simpleName) ?: emptyList()
        testMessagesHolder?.hold(messages)
        fetchRunStats().addExceptionLogging(messages)
    }

    fun setApplicationContext(applicationContext: ApplicationContext) {
        BidragCucumberSingletons.applicationContext = applicationContext
    }

    fun setTestMessagesHolder(testMessagesHolder: TestMessagesHolder) {
        BidragCucumberSingletons.testMessagesHolder = testMessagesHolder
    }
}
