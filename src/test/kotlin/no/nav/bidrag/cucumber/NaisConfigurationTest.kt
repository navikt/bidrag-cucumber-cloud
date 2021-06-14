package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.input.IntegrationInput
import no.nav.bidrag.cucumber.sikkerhet.Sikkerhet.Security
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class NaisConfigurationTest {

    @BeforeEach
    fun `bruk IntegrationInput`() {
        IntegrationInput.use(IntegrationInput(environment = Environment.MAIN_ENVIRONMENT, naisProjectFolder = "src/test/resources"))
    }

    @Test
    fun `skal ikke bruke sikkerhet`() {
        val sikkerTeknologi = NaisConfiguration.read("bidrag-beregn-forskudd-rest")

        assertThat(sikkerTeknologi).isEqualTo(Security.NONE)
    }

    @Test
    fun `skal bruke azure som sikkerhet`() {
        val sikkerTeknologi = NaisConfiguration.read("bidrag-azure-app")

        assertThat(sikkerTeknologi).isEqualTo(Security.AZURE)
    }

    @AfterEach
    fun `reset IntegrationInput`() {
        IntegrationInput.reset()
    }
}
