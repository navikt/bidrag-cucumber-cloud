package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.BidragCucumberCloud.INTEGRATION_INPUT
import no.nav.bidrag.cucumber.BidragCucumberCloud.SANITY_CHECK
import no.nav.bidrag.cucumber.BidragCucumberCloud.TEST_AUTH
import no.nav.bidrag.cucumber.input.IntegrationInput
import no.nav.bidrag.cucumber.input.IntegrationInput.Provider

internal object Environment {
    const val AZURE_LOGIN_ENDPOINT = "https://login.microsoftonline.com"
    const val MAIN_ENVIRONMENT = "main"

    private var integrationInput: IntegrationInput? = null

    fun fetchIntegrationInput() = integrationInput ?: readIntegrationInput()
    internal fun fetchTestUserAuthentication() = fetchPropertyOrEnvironment(TEST_AUTH) ?: throw IllegalStateException("Unable to find '$TEST_AUTH'!")
    internal fun isSanityCheck() = fetchPropertyOrEnvironment(SANITY_CHECK)?.toBoolean() ?: false

    private fun readIntegrationInput(): IntegrationInput {
        return when (IntegrationInput.provider) {
            Provider.FILE -> readWhenNull()
            Provider.INSTANCE -> IntegrationInput.instance ?: throw IllegalStateException("no instance provided")
        }
    }

    private fun readWhenNull(): IntegrationInput {
        if (integrationInput == null) {
            val integrationInput = IntegrationInput.from(fetchPropertyOrEnvironment(INTEGRATION_INPUT))
            this.integrationInput = integrationInput
        }

        return this.integrationInput!!
    }

    private fun fetchPropertyOrEnvironment(key: String) = System.getProperty(key) ?: System.getenv(key)
}
