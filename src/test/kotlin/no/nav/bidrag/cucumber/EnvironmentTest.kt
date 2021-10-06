package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.model.CucumberTestsModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Environment")
internal class EnvironmentTest {
    @Test
    fun `skal hente passord basert på testUsername`() {
        val cucumberTestsModel = CucumberTestsModel(testUsername = "jactor-rises")
        cucumberTestsModel.initCucumberEnvironment()

        System.setProperty("TEST_AUTH_JACTOR-RISES", "007")
        assertThat(Environment.testUserAuth).isEqualTo("007")
    }

    @Test
    fun `skal hente sanity check`() {
        val cucumberTestsModel = CucumberTestsModel(sanityCheck = true)
        cucumberTestsModel.initCucumberEnvironment()

        assertThat(Environment.isSanityCheck).isTrue

        Environment.initCucumberEnvironment(CucumberTestsModel(sanityCheck = false))
        assertThat(Environment.isSanityCheck).isFalse
    }
}
