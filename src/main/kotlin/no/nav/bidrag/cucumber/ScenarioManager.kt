package no.nav.bidrag.cucumber


import io.cucumber.java8.Scenario
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ScenarioManager {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ScenarioManager::class.java)

        private lateinit var correlationIdForScenario: String
        private var scenario: Scenario? = null

        fun use(scenario: Scenario) {
            this.scenario = scenario
            correlationIdForScenario = createCorrelationIdValue()
        }

        fun reset(scenario: Scenario) {
            val noScenario = scenario.name != null && scenario.name.isNotBlank()
            val scenarioString = if (noScenario) "'${scenario.name}'" else "nameless scenario from ${scenario.uri}"

            LOGGER.info("Finished $scenarioString")

            this.scenario = null
            correlationIdForScenario = createCorrelationIdValue("outside-scenario")
        }

        private fun createCorrelationIdValue(label: String = "bcc"): String {
            return "$label-${java.lang.Long.toHexString(System.currentTimeMillis())}"
        }

        fun log(message: String) {
            log(null, message, LogLevel.INFO)
        }

        fun log(messageTitle: String?, message: String) {
            log(messageTitle, message, LogLevel.INFO)
        }

        private fun log(messageTitle: String?, message: String, logLevel: LogLevel) {
            when {
                scenario != null -> {
                    val title = logLevel.produceMessageTitle(messageTitle)
                    scenario!!.log("$title<p>\n$message\n</p>")
                }
                logLevel == LogLevel.INFO -> {
                    LOGGER.info("Outside scenario: $message")
                }
                else -> {
                    LOGGER.error("Outside scenario: $message")
                }
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

        fun createCorrelationIdLinkTitle() = "Link for correlation-id, $correlationIdForScenario"
        fun getCorrelationIdForScenario() = correlationIdForScenario
        fun errorLog(message: String) = log(null, message, LogLevel.ERROR)

        private enum class LogLevel {
            INFO, ERROR;

            fun produceMessageTitle(messageTitle: String?) = if (messageTitle != null && this == INFO)
                "<h5>$messageTitle</h5>\n"
            else if (messageTitle != null && this == ERROR)
                "<h5>An error occured: $messageTitle</h5>\n"
            else if (this == ERROR)
                "<h5>An error occured!</h5>\n"
            else
                ""
        }
    }
}
