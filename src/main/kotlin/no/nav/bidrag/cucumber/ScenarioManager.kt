package no.nav.bidrag.cucumber

import io.cucumber.java8.Scenario
import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.cucumber.model.BidragCucumberSingletons
import no.nav.bidrag.cucumber.model.CucumberTestRun
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ScenarioManager {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(ScenarioManager::class.java)

    private var correlationIdForScenario: String? = null
    private var scenario: Scenario? = null

    fun use(scenario: Scenario) {
        LOGGER.info("Starting ${BidragCucumberSingletons.scenarioMessage(scenario)}")
        this.scenario = scenario
        createCorrelationId()
        MDC.put(CORRELATION_ID, correlationIdForScenario)
    }

    fun reset(scenario: Scenario) {
        LOGGER.info("Finished ${BidragCucumberSingletons.scenarioMessage(scenario)}")
        BidragCucumberSingletons.addRunStats(scenario)
        this.scenario = null
        this.correlationIdForScenario = null
        MDC.clear()
    }

    fun log(message: String) {
        scenario?.log(message)
    }

    private fun createCorrelationId() {
        correlationIdForScenario = createCorrelationIdValue()

        val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = LocalDate.now().format(pattern)

        val time = "time:(from:%27${date}T00:00:00.000Z%27,to:%27${date}T23:59:59.999Z%27)"
        val columns = "columns:!(message,level,application)"
        val index = "index:%2796e648c0-980a-11e9-830a-e17bbd64b4db%27"
        val query = "query:(language:lucene,query:'%22$correlationIdForScenario%22')"
        val sort = "sort:!(!(%27@timestamp%27,desc))"

        LOGGER.info("${linkMessage()}: https://logs.adeo.no/app/kibana#/discover?_g=($time)&_a=($columns,$index,interval:auto,$query,$sort)")
    }

    private fun createCorrelationIdValue(value: String = "bcc"): String {
        return CorrelationId.generateTimestamped(value).get()
    }

    private fun linkMessage() = "Link for correlation-id ($correlationIdForScenario)"
    fun getCorrelationIdForScenario() = correlationIdForScenario ?: createCorrelationIdValue("unknown")
    fun errorLog(message: String, e: Exception) {
        LOGGER.error("$message - ${e.javaClass.simpleName}")
        CucumberTestRun.holdExceptionForTest(e)
    }
}
