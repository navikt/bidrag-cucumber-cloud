package no.nav.bidrag.cucumber.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalStateException
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
        val cucumberTests = CucumberTests(ingressesForApps = listOf("https://somewhere.out.there@tag:bidrag-sak"))

        assertThat(cucumberTests.fetchTags()).`as`("cucumberTests.fetchTags").isEqualTo("(@bidrag-sak and not @ignored)")
    }

    @Test
    fun `skal også bruke tags som ikke er listet i ingressesForApps`() {
        val cucumberTests = CucumberTests(
            ingressesForApps = listOf("https://somewhere.out.there@tag:bidrag-sak"), tags = listOf("@bidrag-arbeidsflyt")
        )

        assertThat(cucumberTests.fetchTags()).`as`("cucumberTests.fetchTags")
            .isEqualTo("(@bidrag-sak and not @ignored) or (@bidrag-arbeidsflyt and not @ignored)")
    }

    @Test
    fun `skal bare plukke tags fra ingressesForApps`() {
        val cucumberTests = CucumberTests(ingressesForApps = listOf("somewhere@tag:bidrag-arbeidsflyt", "here@this-app"))

        assertThat(cucumberTests.fetchTags()).`as`("cucumberTests.fetchTags")
            .isEqualTo("(@bidrag-arbeidsflyt and not @ignored)")
    }

    @Test
    fun `skal feile når tag ikke finnes blant feature files`() {
        val cucumberTests = CucumberTests(ingressesForApps = listOf("somewhere@tag:not-available"))

        assertThatIllegalStateException().isThrownBy { cucumberTests.fetchTags() }
            .withMessageContaining("@not-available er ukjent")
            .withMessageContaining("bidrag-cucumber-cloud.feature")
    }
}
