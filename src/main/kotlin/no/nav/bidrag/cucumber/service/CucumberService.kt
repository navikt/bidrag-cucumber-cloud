package no.nav.bidrag.cucumber.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.cucumber.core.cli.Main
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.cucumber.ABSOLUTE_CLOUD_PATH
import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.hendelse.HendelseProducer
import no.nav.bidrag.cucumber.model.BidragCucumberSingletons
import no.nav.bidrag.cucumber.model.TestMessagesHolder
import no.nav.bidrag.cucumber.model.CucumberTestsDto
import no.nav.bidrag.cucumber.model.SuppressStackTraceText
import no.nav.bidrag.cucumber.model.TestFailedException
import org.slf4j.LoggerFactory
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
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(CucumberService::class.java)
    }

    init {
        BidragCucumberSingletons.hendelseProducer = hendelseProducer
        BidragCucumberSingletons.objectMapper = objectMapper
        BidragCucumberSingletons.setApplicationContext(applicationContext)
        BidragCucumberSingletons.setExceptionLogger(exceptionLogger)
        BidragCucumberSingletons.setTestMessagesHolder(testMessagesHolder)
    }

    internal fun run(cucumberTestsDto: CucumberTestsDto): String {
        Environment.initCucumberEnvironment(cucumberTestsDto)

        val tags = cucumberTestsDto.fetchTags()
        val result = runCucumberTests(tags)

        val suppressedStackText = suppressStackTraceText.suppress(
            BidragCucumberSingletons.fetchTestMessagesWithRunStats()
        )

        Environment.resetCucumberEnvironment()

        if (result != 0.toByte()) {
            val message = "Cucumber tests failed! (tags: $tags)!"
            LOGGER.error(message)
            throw TestFailedException(message, suppressedStackText)
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
