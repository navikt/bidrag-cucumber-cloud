package no.nav.bidrag.cucumber.input

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.io.File

internal class IntegrationInputTest {
    @Test
    fun `skal lese og mappe fra json`() {
        val integrationInput = IntegrationInput.from("src/test/resources/integrationInput.json")

        assertAll(
            { assertThat(integrationInput.environment).`as`("environment").isEqualTo("main") },
            { assertThat(integrationInput.naisProjectFolder).`as`("naisProjectFolder").isEqualTo("src/test/resources") },
            { assertThat(integrationInput.userTest).`as`("taggedTest").isEqualTo("z104364") },
        )
    }

    @Test
    fun `skal lese liste med AzureInput til IntegrationInput`() {
        val azureInputs = IntegrationInput.from("src/test/resources/integrationInput.json").azureInputs

        assertAll(
            { assertThat(azureInputs).`as`("azureInputs").hasSize(1) },
            { assertThat(azureInputs.first().name).`as`("name").isEqualTo("bidrag-azure-app") },
            { assertThat(azureInputs.first().clientId).`as`("clientId").isEqualTo("klient id") },
            { assertThat(azureInputs.first().clientSecret).`as`("clientSecret").isEqualTo("klient hemmelighet") },
            { assertThat(azureInputs.first().tenant).`as`("tenant").isEqualTo("leietaker") }
        )
    }

    @Test
    @DisplayName("skal ha authorityEndpoint hardkoded i AzureInput når den ikke er angitt i json fil")
    fun `skal ha authorityEndpoint hardkoded i AzureInput nar den ikke er angitt i json fil`() {
        val azureInput = IntegrationInput.from("src/test/resources/integrationInput.json").azureInputs.first()
        val jsonText = File("src/test/resources/integrationInput.json").readText(Charsets.UTF_8)

        assertAll(
            { assertThat(azureInput.authorityEndpoint).`as`("authorityEndpoint").isEqualTo("https://login.microsoftonline.com") },
            { assertThat(jsonText).`as`("jsonText").doesNotContain("authorityEndpoint") }
        )
    }
}
