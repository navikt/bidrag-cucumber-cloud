package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.config.IntegrationInput

internal object Environment {
    private var integrationInput: IntegrationInput? = null

    fun fetchIntegrationInput() = integrationInput ?: readIntegrationInput()

    private fun readIntegrationInput(): IntegrationInput {
        val integrationInput = IntegrationInput.fromJson()
        this.integrationInput = integrationInput

        return integrationInput
    }

}
