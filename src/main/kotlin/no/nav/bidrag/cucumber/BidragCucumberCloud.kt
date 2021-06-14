package no.nav.bidrag.cucumber

import io.cucumber.java8.Scenario
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object BidragCucumberCloud {
    // constants for input via System.getProperty()/System.geteenv()
    internal const val INTEGRATION_INPUT: String = "INTEGRATION_INPUT"
    internal const val TEST_AUTH = "TEST_AUTH"

    private val LOGGER = LoggerFactory.getLogger(BidragCucumberCloud::class.java)
    private var scenario: Scenario? = null
    private lateinit var correlationIdForScenario: String

    fun use(scenario: Scenario) {
        this.scenario = scenario
        correlationIdForScenario = createCorrelationIdValue()
    }

    fun reset(scenario: Scenario) {
        LOGGER.info("Finished ${scenario.name}")
        this.scenario = null
        correlationIdForScenario = createCorrelationIdValue("outside-scenario")
    }

    private fun createCorrelationIdValue(label: String = "bcc"): String {
        return "$label-${java.lang.Long.toHexString(System.currentTimeMillis())}"
    }

    fun log(message: String) {
        log(null, message)
    }

    fun log(messageTitle: String?, message: String) {
        if (scenario != null) {
            val title = if (messageTitle != null) "<h5>$messageTitle</h5>\n" else ""
            scenario!!.log("$title<p>\n$message\n</p>")
        } else {
            LOGGER.info("Logging message outside scenario: $message")
        }
    }

    fun createQueryLinkForCorrelationId(): String {
        val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = LocalDate.now().format(pattern)

        val time = "time:(from:%27${date}T00:00:00.000Z%27,to:%27${date}T23:59:59.999Z%27)"
        val columns = "columns:!(message,level,application)"
        val index = "index:%2796e648c0-980a-11e9-830a-e17bbd64b4db%27"
        val query = "query:(language:lucene,query:'x_correlationId:%22$correlationIdForScenario%22')"
        val sort = "sort:!(!(%27@timestamp%27,desc))"

        return "https://logs.adeo.no/app/kibana#/discover?_g=($time)&_a=($columns,$index,interval:auto,$query,$sort)"
    }

    fun createCorrelationIdLinkTitle() = "Link for correlation-id ($correlationIdForScenario):"
    fun getCorrelationIdForScenario() = correlationIdForScenario
}
