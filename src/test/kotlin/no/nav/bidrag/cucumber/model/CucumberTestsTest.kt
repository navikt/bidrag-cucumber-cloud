package no.nav.bidrag.cucumber.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("CucumberTestsDto")
internal class CucumberTestsTest {

    @Test
    fun `skal ha verdien false som streng når sanityCheck er null`() {
        val cucumberTests = CucumberTests(sanityCheck = null)

        assertThat(cucumberTests.getSanityCheck()).isEqualTo("false")
    }

    @Test
    fun `skal hente tags basert på ingressesForApps`() {
        val cucumberTests = CucumberTests(ingressesForApps = listOf("https://somewhere.out.there@bidrag-sak"))

        assertThat(cucumberTests.fetchTags()).`as`("cucumberTests.fetchTags").isEqualTo("(@bidrag-sak and not @ignored)")
    }

    @Test
    fun `skal også bruke tags som ikke er listet i ingressesForApps`() {
        val cucumberTests = CucumberTests(
            ingressesForApps = listOf("https://somewhere.out.there@bidrag-sak"), tags = listOf("@bidrag-arbeidsflyt")
        )

        assertThat(cucumberTests.fetchTags()).`as`("cucumberTests.fetchTags")
            .isEqualTo("(@bidrag-sak and not @ignored) or (@bidrag-arbeidsflyt and not @ignored)")
    }

    @Test
    fun `skal bare plukke tags fra ingressesForApps`() {
        val cucumberTests = CucumberTests(ingressesForApps = listOf("somewhere@bidrag-arbeidsflyt", "here@no-tag:this-app"))

        assertThat(cucumberTests.fetchTags()).`as`("cucumberTests.fetchTags")
            .isEqualTo("(@bidrag-arbeidsflyt and not @ignored)")
    }

    @Test
    fun `skal feile når tag ikke finnes blant feature files`() {
        val cucumberTests = CucumberTests(ingressesForApps = listOf("somewhere@not-available"))

        assertThatIllegalStateException().isThrownBy { cucumberTests.fetchTags() }
            .withMessageContaining("@not-available er ukjent")
            .withMessageContaining("bidrag-cucumber-cloud.feature")
    }

    @Test
    fun `skal feile hvis det ikke finnes noen tags`() {
        val cucumberTests = CucumberTests(ingressesForApps = listOf("shit@no-tag:not-available"), tags = emptyList())

        assertThatIllegalStateException().isThrownBy { cucumberTests.fetchTags() }
            .withMessage("Ingen tags er oppgitt. Bruk liste med tags eller liste med ingresser som ikke har prefiksen 'no-tag:' etter @")
    }
}
