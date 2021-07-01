package no.nav.bidrag.cucumber

import io.cucumber.core.cli.Main
import io.cucumber.java8.Scenario
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object BidragCucumberCloud {
    // constants for input via System.getProperty(...)/System.getenv(...)
    internal const val AZURE_APP_CLIENT_ID = "AZURE_APP_CLIENT_ID"
    internal const val AZURE_APP_CLIENT_SECRET = "AZURE_APP_CLIENT_SECRET"
    internal const val AZURE_APP_TENANT_ID = "AZURE_APP_TENANT_ID"
    internal const val TEST_AUTH = "TEST_AUTH"
    internal const val TEST_INGRESSES = "TEST_INGRESSES"
    internal const val TEST_USER = "TEST_USER"
    internal const val SANITY_CHECK = "SANITY_CHECK"

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
        log(null, message, LogLevel.INFO)
    }

    fun log(messageTitle: String?, message: String) {
        log(messageTitle, message, LogLevel.INFO)
    }

    private fun  log(messageTitle: String?, message: String, logLevel: LogLevel) {
        if (scenario != null) {
            val title = logLevel.produceMessageTitle(messageTitle)
            scenario!!.log("$title<p>\n$message\n</p>")
        } else if (logLevel == LogLevel.INFO) {
            LOGGER.info("Outside scenario: $message")
        } else {
            LOGGER.error("Outside scenario: $message")
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

    @JvmStatic
    fun main(args: Array<String>) {
        val tags = if (args.isEmpty()) "not @ignored" else args.joinToString(prefix = "(@", postfix = " and not @ignore)", separator = " or ")

        val result = Main.run(
            "src/test/resources/no/nav/bidrag/cucumber/cloud", "--glue", "no.nav.bidrag.cucumber.cloud", "--tags", tags
        )

        if (result != 0.toByte()) {
            val message = "Kjøring av cucumber var mislykket (tags: $tags)!"
            LOGGER.error(message)
            throw IllegalStateException(message)
        }
    }
}
