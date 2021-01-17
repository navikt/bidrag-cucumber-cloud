package no.nav.bidrag.cucumber

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import java.io.File

private val LOGGER = LoggerFactory.getLogger(IntegrationInput::class.java)

class IntegrationInput(
    var azureInputs: List<AzureInput> = emptyList(),
    var environment: String = "<not set>",
    var naisProjectFolder: String = "<not set>",
    var taggedTest: String? = null,
    var userNav: String = "<not set>",
    var userNavAuth: String = "<not set>",
    var userTest: String = "<not set>",
    var userTestAuth: String = "<not set>"
) {
    companion object {
        internal var provider = Provider.FILE
        internal var instance: IntegrationInput? = null

        fun fromJson(): IntegrationInput {
            val filePath = System.getProperty(INTEGRATION_INPUT) ?: System.getenv(INTEGRATION_INPUT)

            return when (provider) {
                Provider.FILE -> readJsonFile(filePath ?: throw IllegalStateException("Fant ikke angitt json-path: $filePath"))
                Provider.INSTANCE -> instance
            } ?: throw IllegalStateException("Unable to find IntegrationInput: $filePath")
        }

        private fun readJsonFile(filePath: String): IntegrationInput? {
            LOGGER.info("Will try to read environment file from $filePath")
            val json = File(filePath).inputStream().readBytes().toString(Charsets.UTF_8)
            return ObjectMapper().readValue(json, IntegrationInput::class.java)
        }

        internal fun use(integrationInput: IntegrationInput) {
            provider = Provider.INSTANCE
            instance = integrationInput
        }

        internal fun reset() {
            provider = Provider.FILE
            instance = null
        }
    }

    fun fetchAzureInput(applicationName: String): AzureInput {
        return azureInputs.find { it.name == applicationName } ?: throw IllegalStateException("Fant ikke azureInputs for $applicationName")
    }

    fun fetchTenantUsername(): String {
        val testUserUpperCase = userTest.toUpperCase()
        return "F_$testUserUpperCase.E_$testUserUpperCase@trydeetaten.no"
    }
}

class AzureInput(
    var authorityEndpoint: String = "https://login.microsoftonline.com",
    var clientId: String = "<not set>",
    var clientSecret: String = "<not set>",
    var name: String = "<not set>",
    var tenant: String = "<not set>"
)

internal enum class Provider {
    FILE, INSTANCE
}
