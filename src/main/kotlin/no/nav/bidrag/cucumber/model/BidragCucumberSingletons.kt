package no.nav.bidrag.cucumber.model

import com.fasterxml.jackson.databind.ObjectMapper
import io.cucumber.java8.Scenario
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.SpringConfig
import no.nav.bidrag.cucumber.hendelse.HendelseProducer
import no.nav.bidrag.cucumber.hendelse.JournalpostHendelse
import no.nav.bidrag.cucumber.sikkerhet.SecurityTokenService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext

/**
 * Singletons som er gyldige i en cucumber-kjøring og som er felles for ALLE egenskaper definert i feature-filer
 */
internal object BidragCucumberSingletons {
    @JvmStatic
    private val RUN_STATS = ThreadLocal<RunStats>()

    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(BidragCucumberSingletons::class.java)

    private var applicationContext: ApplicationContext? = null
    private var exceptionLogger: ExceptionLogger? = null
    private var hendelseProducer: HendelseProducer? = null
    private var objectMapper: ObjectMapper? = null
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
        val haveScenario = scenario.name != null && scenario.name.isNotBlank()
        return if (haveScenario) "'${scenario.name}'" else "scenario in ${scenario.uri}"
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

    fun holdExceptionForTest(throwable: Throwable) {
        val messages = exceptionLogger?.logException(throwable, BidragCucumberSingletons::class.java.simpleName) ?: listOf(
            "${throwable.javaClass.simpleName}: ${throwable.message}"
        )

        testMessagesHolder?.hold(messages)
        fetchRunStats().addExceptionLogging(messages)
    }

    fun publiserHendelse(journalpostHendelse: JournalpostHendelse) {
        hendelseProducer?.publish(journalpostHendelse) ?: LOGGER.warn(
            "Cannot publish ${journalpostHendelse.hendelse} when spring context is not initialized, sanity check: ${Environment.isSanityCheck}"
        )
    }

    fun <T> readValue(value: String, mapClass: Class<T>): T = objectMapper?.readValue(value, mapClass) ?: throw IllegalStateException(
        "Kunne ikke mappe: $value"
    )

    fun toJson(body: Any): String = objectMapper?.writeValueAsString(body) ?: throw IllegalStateException(
        "har ikke fått jackson objectMapper fra spring!"
    )

    fun setApplicationContext(applicationContext: ApplicationContext) {
        BidragCucumberSingletons.applicationContext = applicationContext
    }

    fun setExceptionLogger(exceptionLogger: ExceptionLogger) {
        BidragCucumberSingletons.exceptionLogger = exceptionLogger
    }

    fun setHendelseProducer(hendelseProducer: HendelseProducer) {
        BidragCucumberSingletons.hendelseProducer = hendelseProducer
    }

    fun setObjectMapper(objectMapper: ObjectMapper) {
        BidragCucumberSingletons.objectMapper = objectMapper
    }

    fun setTestMessagesHolder(testMessagesHolder: TestMessagesHolder) {
        BidragCucumberSingletons.testMessagesHolder = testMessagesHolder
    }
}
