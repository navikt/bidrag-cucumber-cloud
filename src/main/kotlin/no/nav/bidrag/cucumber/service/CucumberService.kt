package no.nav.bidrag.cucumber.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.cucumber.core.cli.Main
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.cucumber.ABSOLUTE_CLOUD_PATH
import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.hendelse.HendelseProducer
import no.nav.bidrag.cucumber.model.BidragCucumberSingletons
import no.nav.bidrag.cucumber.model.CucumberTestsModel
import no.nav.bidrag.cucumber.model.SuppressStackTraceText
import no.nav.bidrag.cucumber.model.TestFailedException
import no.nav.bidrag.cucumber.model.TestMessagesHolder
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class CucumberService(
    private val suppressStackTraceText: SuppressStackTraceText,
    applicationContext: ApplicationContext,
    exceptionLogger: ExceptionLogger,
    hendelseProducer: HendelseProducer,
    objectMapper: ObjectMapper,
    testMessagesHolder: TestMessagesHolder
) {
    init {
        BidragCucumberSingletons.setApplicationContext(applicationContext)
        BidragCucumberSingletons.setExceptionLogger(exceptionLogger)
        BidragCucumberSingletons.setHendelseProducer(hendelseProducer)
        BidragCucumberSingletons.setObjectMapper(objectMapper)
        BidragCucumberSingletons.setTestMessagesHolder(testMessagesHolder)
    }

    internal fun run(cucumberTestsModel: CucumberTestsModel): String {
        Environment.initCucumberEnvironment(cucumberTestsModel)

        val tags = cucumberTestsModel.fetchTags()
        val result = runCucumberTests(tags)

        val suppressedStackText = suppressStackTraceText.suppress(
            BidragCucumberSingletons.fetchTestMessagesWithRunStats()
        )

        Environment.resetCucumberEnvironment()

        if (result != 0.toByte()) {
            throw TestFailedException("Cucumber tests failed! (tags: $tags)!", suppressedStackText)
        }

        return suppressedStackText
    }

    private fun runCucumberTests(tags: String): Byte {
        if (tags.isBlank()) throw IllegalStateException("Ingen tags som kan brukes")

        return Main.run(
            ABSOLUTE_CLOUD_PATH, "--glue", "no.nav.bidrag.cucumber.cloud", "--tags", tags
        )
    }
}
