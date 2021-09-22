package no.nav.bidrag.cucumber

import io.cucumber.java8.Scenario
import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.cucumber.model.BidragCucumberSingletons
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ScenarioManager {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(ScenarioManager::class.java)

    private lateinit var correlationIdForScenario: String
    private var scenario: Scenario? = null

    fun use(scenario: Scenario) {
        this.scenario = scenario
        initCorrelationId()
        LOGGER.info("Starting ${BidragCucumberSingletons.scenarioMessage(scenario)}")
    }

    internal fun initCorrelationId() {
        correlationIdForScenario = createCorrelationIdValue()
        MDC.put(CORRELATION_ID, correlationIdForScenario)
    }

    fun reset(scenario: Scenario) {
        LOGGER.info("Finished ${BidragCucumberSingletons.scenarioMessage(scenario)}")
        BidragCucumberSingletons.addRunStats(scenario)
        this.scenario = null
        resetCorrelationId()
    }

    private fun resetCorrelationId() {
        correlationIdForScenario = createCorrelationIdValue("outside-scenario")
        MDC.clear()
    }

    private fun createCorrelationIdValue(label: String = "bcc"): String {
        return CorrelationId.generateTimestamped(label).get()
    }

    fun log(messageTitle: String, message: String) {
        val testMessage = "$messageTitle: $message"

        if (scenario != null) {
            scenario!!.log("$testMessage\n")
            BidragCucumberSingletons.holdTestMessage(testMessage)
        } else {
            LOGGER.info("Outside scenario: $testMessage")
        }
    }

    fun createQueryLinkForCorrelationId(): String {
        val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = LocalDate.now().format(pattern)

        val time = "time:(from:%27${date}T00:00:00.000Z%27,to:%27${date}T23:59:59.999Z%27)"
        val columns = "columns:!(message,level,application)"
        val index = "index:%2796e648c0-980a-11e9-830a-e17bbd64b4db%27"
        val query = "query:(language:lucene,query:'%22$correlationIdForScenario%22')"
        val sort = "sort:!(!(%27@timestamp%27,desc))"

        return "https://logs.adeo.no/app/kibana#/discover?_g=($time)&_a=($columns,$index,interval:auto,$query,$sort)"
    }

    fun createCorrelationIdLinkTitle() = "Link for correlation-id, $correlationIdForScenario"
    fun getCorrelationIdForScenario() = correlationIdForScenario
    fun errorLog(message: String, e: Exception) {
        LOGGER.error("$message - ${e.javaClass.simpleName}")
        BidragCucumberSingletons.holdExceptionForTest(e)
    }
}
