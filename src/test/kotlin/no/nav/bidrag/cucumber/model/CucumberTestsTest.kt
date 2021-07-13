package no.nav.bidrag.cucumber.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("CucumberTests")
internal class CucumberTestsTest {

    @Test
    fun `skal ha verdien false som streng når sanityCheck er null`() {
        val cucumberTests = CucumberTests()
        cucumberTests.sanityCheck = null

        assertThat(cucumberTests.getSanityCheck()).isEqualTo("false")
    }
}