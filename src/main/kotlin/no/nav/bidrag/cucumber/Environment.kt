package no.nav.bidrag.cucumber

internal object Environment {
    private var integrationInput: IntegrationInput? = null

    fun fetchIntegrationInput() = integrationInput ?: readIntegrationInput()

    private fun readIntegrationInput(): IntegrationInput {
        return when (IntegrationInput.provider) {
            Provider.FILE -> readWhenNull()
            Provider.INSTANCE -> IntegrationInput.instance ?: throw IllegalStateException("no instance provided")
        }
    }

    private fun readWhenNull(): IntegrationInput {
        if (integrationInput == null) {
            val integrationInput = IntegrationInput.fromJson()
            this.integrationInput = integrationInput
        }

        return this.integrationInput!!
    }
}
