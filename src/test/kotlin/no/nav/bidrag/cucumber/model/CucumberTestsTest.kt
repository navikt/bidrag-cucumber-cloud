package no.nav.bidrag.cucumber.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("CucumberTests")
internal class CucumberTestsTest {

    @Test
    fun `skal ha verdien false som streng når sanityCheck er null`() {
        val cucumberTests = CucumberTests(sanityCheck = null)

        assertThat(cucumberTests.getSanityCheck()).isEqualTo("false")
    }

    @Test
    fun `skal hente tags basert på ingressesForTags`() {
        val cucumberTests = CucumberTests(ingressesForTags = listOf("https://somewhere.out.there@my-tag"))

        assertThat(cucumberTests.fetchTags()).`as`("cucumberTests.fetchTags").isEqualTo("(@my-tag and not @ignored)")
    }
}
