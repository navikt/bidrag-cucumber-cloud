package no.nav.bidrag.cucumber.config

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.cucumber.INTEGRATION_INPUT
import java.io.File

class IntegrationInput(
    var azureAppNames: List<String> = emptyList(),
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
}

enum class Provider {
    FILE, INSTANCE
}
