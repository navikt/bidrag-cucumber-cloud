package no.nav.bidrag.cucumber.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("CucumberTestsDto")
internal class CucumberTestsModelTest {

    @Test
    fun `skal ha verdien false som streng når sanityCheck er null`() {
        val cucumberTestsModel = CucumberTestsModel(sanityCheck = null)

        assertThat(cucumberTestsModel.getSanityCheck()).isEqualTo("false")
    }

    @Test
    fun `skal hente tags basert på ingressesForApps`() {
        val cucumberTestsModel = CucumberTestsModel(ingressesForApps = listOf("https://somewhere.out.there@bidrag-sak"))

        assertThat(cucumberTestsModel.fetchTags()).`as`("cucumberTests.fetchTags").isEqualTo("@bidrag-sak and not @ignored")
    }

    @Test
    fun `skal også bruke tags som ikke er listet i ingressesForApps`() {
        val cucumberTestsModel = CucumberTestsModel(
            ingressesForApps = listOf("https://somewhere.out.there@bidrag-sak"), tags = listOf("@arbeidsflyt-endre-fagomrade")
        )

        assertThat(cucumberTestsModel.fetchTags()).`as`("cucumberTests.fetchTags")
            .isEqualTo("(@bidrag-sak or @arbeidsflyt-endre-fagomrade) and not @ignored")
    }

    @Test
    fun `skal bare plukke tags fra ingressesForApps`() {
        val cucumberTestsModel = CucumberTestsModel(ingressesForApps = listOf("somewhere@arbeidsflyt-endre-fagomrade", "here@no-tag:this-app"))

        assertThat(cucumberTestsModel.fetchTags()).`as`("cucumberTests.fetchTags")
            .isEqualTo("@arbeidsflyt-endre-fagomrade and not @ignored")
    }

    @Test
    fun `skal feile når tag ikke finnes blant feature files`() {
        val cucumberTestsModel = CucumberTestsModel(ingressesForApps = listOf("somewhere@not-available"))

        assertThatIllegalStateException().isThrownBy { cucumberTestsModel.fetchTags() }
            .withMessageContaining("@not-available er ukjent")
            .withMessageContaining("bidrag-cucumber-cloud.feature")
    }

    @Test
    fun `skal feile hvis det ikke finnes noen tags`() {
        val cucumberTestsModel = CucumberTestsModel(ingressesForApps = listOf("shit@no-tag:not-available"), tags = emptyList())

        assertThatIllegalStateException().isThrownBy { cucumberTestsModel.fetchTags() }
            .withMessage("Ingen tags er oppgitt. Bruk liste med tags eller liste med ingresser som ikke har prefiksen 'no-tag:' etter @")
    }

    @Test
    fun `skal ikke hente ut tags dobbelt opp`() {
        val cucumberTestsModel = CucumberTestsModel(ingressesForApps = listOf("https://somewhere.out.there@bidrag-sak"), tags = listOf("@bidrag-sak"))

        assertThat(cucumberTestsModel.fetchTags()).`as`("cucumberTests.fetchTags").isEqualTo("@bidrag-sak and not @ignored")
    }
}
