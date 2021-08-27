package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.model.CucumberTestsDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Environment")
internal class EnvironmentTest {
    @Test
    fun `skal hente passord basert på testUsername`() {
        val cucumberTestsDto = CucumberTestsDto(testUsername = "jactor-rises")
        cucumberTestsDto.initCucumberEnvironment()

        System.setProperty("TEST_AUTH_JACTOR-RISES", "007")
        assertThat(Environment.testUserAuth).isEqualTo("007")
    }

    @Test
    fun `skal hente sanity check`() {
        val cucumberTestsDto = CucumberTestsDto(sanityCheck = true)
        cucumberTestsDto.initCucumberEnvironment()

        assertThat(Environment.isSanityCheck).isTrue

        cucumberTestsDto.sanityCheck = false
        assertThat(Environment.isSanityCheck).isFalse
    }
}
