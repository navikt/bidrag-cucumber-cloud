package no.nav.bidrag.cucumber.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("CucumberTests")
internal class CucumberTestsDtoTest {

    @Test
    fun `skal ha verdien false som streng når sanityCheck er null`() {
        val cucumberTestsDto = CucumberTestsDto(sanityCheck = null)

        assertThat(cucumberTestsDto.getSanityCheck()).isEqualTo("false")
    }

    @Test
    fun `skal hente tags basert på ingressesForApps`() {
        val cucumberTestsDto = CucumberTestsDto(ingressesForApps = listOf("https://somewhere.out.there@tag:bidrag-sak"))

        assertThat(cucumberTestsDto.fetchTags()).`as`("cucumberTests.fetchTags").isEqualTo("(@bidrag-sak and not @ignored)")
    }

    @Test
    fun `skal også bruke tags som ikke er listet i ingressesForApps`() {
        val cucumberTestsDto = CucumberTestsDto(
            ingressesForApps = listOf("https://somewhere.out.there@tag:bidrag-sak"), tags = listOf("@bidrag-arbeidsflyt")
        )

        assertThat(cucumberTestsDto.fetchTags()).`as`("cucumberTests.fetchTags")
            .isEqualTo("(@bidrag-sak and not @ignored) or (@bidrag-arbeidsflyt and not @ignored)")
    }

    @Test
    fun `skal bare plukke tags fra ingressesForApps`() {
        val cucumberTestsDto = CucumberTestsDto(ingressesForApps = listOf("somewhere@tag:bidrag-arbeidsflyt", "here@this-app"))

        assertThat(cucumberTestsDto.fetchTags()).`as`("cucumberTests.fetchTags")
            .isEqualTo("(@bidrag-arbeidsflyt and not @ignored)")
    }

    @Test
    fun `skal feile når tag ikke finnes blant feature files`() {
        val cucumberTestsDto = CucumberTestsDto(ingressesForApps = listOf("somewhere@tag:not-available"))

        assertThatIllegalStateException().isThrownBy { cucumberTestsDto.fetchTags() }
            .withMessageContaining("@not-available er ukjent")
            .withMessageContaining("bidrag-cucumber-cloud.feature")
    }

    @Test
    fun `skal feile hvis det ikke finnes noen tags`() {
        val cucumberTestsDto = CucumberTestsDto(ingressesForApps = listOf("shit@not-available"), tags = emptyList())

        assertThatIllegalStateException().isThrownBy { cucumberTestsDto.fetchTags() }
            .withMessage("Ingen tags er oppgitt. Bruk liste med tags eller liste med ingresser og prefiks app med 'tag:'")
    }
}
