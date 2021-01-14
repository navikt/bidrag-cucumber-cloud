package no.nav.bidrag.cucumber

import io.cucumber.java8.Scenario
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// constants for input via System.getProperty()/System.geteenv()
internal const val CREDENTIALS_TEST_USER = "TEST_USER"
internal const val CREDENTIALS_TEST_USER_AUTH = "TEST_AUTH"
internal const val CREDENTIALS_USERNAME = "USERNAME"
internal const val CREDENTIALS_USER_AUTH = "USER_AUTH"
internal const val ENVIRONMENT = "ENVIRONMENT"
internal const val PROJECT_NAIS_FOLDER = "PROJECT_NAIS_FOLDER"

// constants for code
internal const val ALIAS_BIDRAG_UI = "bidrag-ui"

// Headers
internal const val X_ENHET_HEADER = "X-Enhet"

private val LOGGER = LoggerFactory.getLogger(BidragCucumberNais::class.java)

open class BidragCucumberNais {

    companion object {
        private var scenario: Scenario? = null
        private var correlationIdForScenario: String = createCorrelationIdValue()

        fun use(scenario: Scenario) {
            this.scenario = scenario
            correlationIdForScenario = createCorrelationIdValue()
        }

        private fun createCorrelationIdValue(): String {
            return "cucumber-nais-${java.lang.Long.toHexString(System.currentTimeMillis())}"
        }

        fun log(message: String) {
            log(null, message)
        }

        fun log(messageTitle: String?, message: String) {
            if (scenario != null) {
                val title = if (messageTitle != null) "<h5>$messageTitle</h5>" else ""
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
}
