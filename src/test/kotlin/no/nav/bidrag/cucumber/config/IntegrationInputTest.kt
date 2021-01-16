package no.nav.bidrag.cucumber.config

import no.nav.bidrag.cucumber.INTEGRATION_INPUT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.io.File

internal class IntegrationInputTest {
    @Test
    fun `skal lese og mappe fra json`() {
        System.setProperty(INTEGRATION_INPUT, "src/test/resources/integrationInput.json")
        val integrationInput = IntegrationInput.fromJson()

        assertAll(
            { assertThat(integrationInput.azureAppNames).`as`("azureAppNames").contains("bidrag-azure-app") },
            { assertThat(integrationInput.environment).`as`("environment").isEqualTo("main") },
            { assertThat(integrationInput.naisProjectFolder).`as`("naisProjectFolder").isEqualTo("src/test/resources") },
            { assertThat(integrationInput.taggedTest).`as`("taggedTest").isEqualTo("bidrag-azure-app") },
            { assertThat(integrationInput.userNav).`as`("taggedTest").isEqualTo("j104364") },
            { assertThat(integrationInput.userNavAuth).`as`("taggedTest").isEqualTo("svada") },
            { assertThat(integrationInput.userTest).`as`("taggedTest").isEqualTo("z104364") },
            { assertThat(integrationInput.userTestAuth).`as`("taggedTest").isEqualTo("lada") }
        )
    }
}
