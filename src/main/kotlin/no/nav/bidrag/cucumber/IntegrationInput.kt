package no.nav.bidrag.cucumber

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

class IntegrationInput(
    var azureInputs: List<AzureInput> = emptyList(),
    var environment: String = "not added",
    var naisProjectFolder: String = "not added",
    var taggedTest: String? = null,
    var userNav: String = "not added",
    var userNavAuth: String = "not added",
    var userTest: String = "not added",
    var userTestAuth: String = "not added"
) {
    companion object {
        internal var provider = Provider.FILE
        internal var instance: IntegrationInput? = null

        fun fromJson(): IntegrationInput {
            val filePath = System.getProperty(INTEGRATION_INPUT) ?: System.getenv(INTEGRATION_INPUT)

            return when (provider) {
                Provider.FILE -> readJsonFile(filePath ?: throw IllegalStateException("Fant ikke json-path"))
                Provider.INSTANCE -> instance
            } ?: throw IllegalStateException("Unablre to find IntegrationInput: $filePath")
        }

        private fun readJsonFile(filePath: String): IntegrationInput? {
            val json = File(filePath).inputStream().readBytes().toString(Charsets.UTF_8)
            return ObjectMapper().readValue(json, IntegrationInput::class.java)
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
    var clientId: String = "not added",
    var clientSecret: String = "not added",
    var name: String = "not added",
    var tenant: String = "not added"
)

internal enum class Provider {
    FILE, INSTANCE
}
