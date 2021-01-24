package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.BidragCucumberNais.INTEGRATION_INPUT
import no.nav.bidrag.cucumber.BidragCucumberNais.TEST_AUTH
import no.nav.bidrag.cucumber.input.IntegrationInput
import no.nav.bidrag.cucumber.input.Provider

internal object Environment {
    private var integrationInput: IntegrationInput? = null

    fun fetchIntegrationInput() = integrationInput ?: readIntegrationInput()
    fun fetchTestUserAuthentication() = fetchExistingPropertyOrEnvironment(TEST_AUTH, "Unable to find '$TEST_AUTH'!")

    private fun readIntegrationInput(): IntegrationInput {
        return when (IntegrationInput.provider) {
            Provider.FILE -> readWhenNull()
            Provider.INSTANCE -> IntegrationInput.instance ?: throw IllegalStateException("no instance provided")
        }
    }

    @Suppress("SameParameterValue")
    private fun fetchExistingPropertyOrEnvironment(key: String, errorMassage: String): String {
        return fetchPropertyOrEnvironment(key) ?: throw IllegalStateException(errorMassage)
    }

    private fun readWhenNull(): IntegrationInput {
        if (integrationInput == null) {
            val integrationInput = IntegrationInput.from(fetchPropertyOrEnvironment(INTEGRATION_INPUT))
            this.integrationInput = integrationInput
        }

        return this.integrationInput!!
    }

    private fun fetchPropertyOrEnvironment(key: String): String? {
        return System.getProperty(key) ?: System.getenv(key)
    }
}
