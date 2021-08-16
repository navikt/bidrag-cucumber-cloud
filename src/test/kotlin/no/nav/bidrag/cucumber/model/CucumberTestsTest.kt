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
    fun `skal hente tags basert på ingressesForApps`() {
        val cucumberTests = CucumberTests(ingressesForApps = listOf("https://somewhere.out.there@tag:my-tag"))

        assertThat(cucumberTests.fetchTags()).`as`("cucumberTests.fetchTags").isEqualTo("(@my-tag and not @ignored)")
    }

    @Test
    fun `skal også bruke tags som ikke er listet i ingressesForApps`() {
        val cucumberTests = CucumberTests(ingressesForApps = listOf("https://somewhere.out.there@tag:my-app"), tags = listOf("@my-tag"))

        assertThat(cucumberTests.fetchTags()).`as`("cucumberTests.fetchTags")
            .isEqualTo("(@my-app and not @ignored) or (@my-tag and not @ignored)")
    }

    @Test
    fun `skal bare plukke tags fra ingressesForApps`() {
        val cucumberTests = CucumberTests(ingressesForApps = listOf("somewhere@tag:my-app", "here@this-app"))

        assertThat(cucumberTests.fetchTags()).`as`("cucumberTests.fetchTags")
            .isEqualTo("(@my-app and not @ignored)")
    }
}
