package no.nav.bidrag.cucumber.service

import io.cucumber.core.cli.Main
import no.nav.bidrag.cucumber.ABSOLUTE_CLOUD_PATH
import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.model.CucumberTests
import no.nav.bidrag.cucumber.model.TestFailedException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TestService {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(TestService::class.java)
    }

    internal fun run(cucumberTests: CucumberTests) {
        cucumberTests.initTestEnvironment()
        val tags = cucumberTests.fetchTags()

        if (tags.isBlank()) throw IllegalStateException("Ingen tags som kan brukes")

        val result = Main.run(
            ABSOLUTE_CLOUD_PATH, "--glue", "no.nav.bidrag.cucumber.cloud", "--tags", tags
        )

        Environment.resetTestEnvironment()

        if (result != 0.toByte()) {
            val message = "Kjøring av cucumber var mislykket (tags: $tags)!"
            LOGGER.error(message)
            throw TestFailedException(message)
        }
    }
}
