package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.model.CucumberTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Environment")
internal class EnvironmentTest {
    @Test
    fun `skal hente passord basert på testUsername`() {
        val cucumberTests = CucumberTests(testUsername = "jactor-rises")
        cucumberTests.initCucumberEnvironment()

        System.setProperty("TEST_AUTH_JACTOR-RISES", "007")
        assertThat(Environment.testUserAuth).isEqualTo("007")
    }

    @Test
    fun `skal hente sanity check`() {
        val cucumberTests = CucumberTests(sanityCheck = true)
        cucumberTests.initCucumberEnvironment()

        assertThat(Environment.isSanityCheck).isTrue

        cucumberTests.sanityCheck = false
        assertThat(Environment.isSanityCheck).isFalse
    }
}
