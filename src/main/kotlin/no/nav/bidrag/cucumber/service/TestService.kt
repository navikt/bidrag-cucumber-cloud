package no.nav.bidrag.cucumber.service

import io.cucumber.core.cli.Main
import no.nav.bidrag.cucumber.ABSOLUTE_CLOUD_PATH
import no.nav.bidrag.cucumber.INGRESSES_FOR_TAGS
import no.nav.bidrag.cucumber.SANITY_CHECK
import no.nav.bidrag.cucumber.SECURITY_TOKEN
import no.nav.bidrag.cucumber.TEST_USER
import no.nav.bidrag.cucumber.model.CucumberTests
import no.nav.bidrag.cucumber.model.IngressesAndTags
import no.nav.bidrag.cucumber.model.TestFailedException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.IllegalStateException

@Service
class TestService(private val ingressesAndTags: IngressesAndTags) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(TestService::class.java)
    }

    fun run(cucumberTests: CucumberTests) {
        ingressesAndTags.clearIngressCache()
        clearOldSystemProperties()
        setNewSystemProperties(cucumberTests)

        run(ingressesAndTags.fetchTags())
    }

    private fun clearOldSystemProperties() {
        System.clearProperty(INGRESSES_FOR_TAGS)
        System.clearProperty(SANITY_CHECK)
        System.clearProperty(SECURITY_TOKEN)
        System.clearProperty(TEST_USER)
    }

    private fun setNewSystemProperties(cucumberTests: CucumberTests) {
        System.setProperty(INGRESSES_FOR_TAGS, cucumberTests.ingressesForTagsAsString())
        System.setProperty(SANITY_CHECK, cucumberTests.getSanityCheck())

        if (cucumberTests.securityToken != null) {
            System.setProperty(SECURITY_TOKEN, cucumberTests.securityToken!!)
        }

        if (cucumberTests.hasTestUsername()) {
            System.setProperty(TEST_USER, cucumberTests.testUsername!!)
        }
    }

    internal fun run(tags: String) {
        if (tags.isBlank()) throw IllegalStateException("Ingen tags som kan brukes")

        val result = Main.run(
            ABSOLUTE_CLOUD_PATH, "--glue", "no.nav.bidrag.cucumber.cloud", "--tags", tags
        )

        if (result != 0.toByte()) {
            val message = "Kjøring av cucumber var mislykket (tags: $tags)!"
            LOGGER.error(message)
            throw TestFailedException(message)
        }
    }
}
