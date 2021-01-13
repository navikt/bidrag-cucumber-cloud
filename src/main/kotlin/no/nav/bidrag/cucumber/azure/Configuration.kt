package no.nav.bidrag.cucumber.azure

import com.natpryce.konfig.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException

private val LOGGER = LoggerFactory.getLogger(Configuration::class.java)

private val defaultProperties = ConfigurationMap(
    mapOf(
        "AZURE_AUTHORITY_ENDPOINT" to "https://login.microsoftonline.com",
        "AZURE_CLIENT_ID" to "61137106-058f-4b4c-8a65-11911f1bdd8f",
        "AZURE_TENANT" to "966ac572-f5b7-4bbe-aa88-c76419c0f851",
        "BIDRAG_SAK_BASE_URL" to "https://bidrag-sak-feature.dev.adeo.no/bidrag-sak",
        "USERNAME" to "F_Z991656.E_Z991656@trygdeetaten.no",
        "PASSWORD" to "Sjonkel8"
    )
)

private val config = ConfigurationProperties.systemProperties() overriding
        EnvironmentVariables overriding
        defaultProperties

private fun String.configProperty(): String = config[Key(this, stringType)]

private fun String.readFile() =
    try {
        File(this).readText(Charsets.UTF_8)
    } catch (err: FileNotFoundException) {
        LOGGER.warn("Azure fil $this ikke funnet")
        null
    }

data class Configuration(
    val azureAd: AzureAd = AzureAd(),
    val bidragSakBaseUrl: String = "BIDRAG_SAK_BASE_URL".configProperty(),
    val password: String = "PASSWORD".configProperty(),
) {

    data class AzureAd(
        val clientId: String = "/var/run/secrets/nais.io/azuread/client_id".readFile()
            ?: "AZURE_CLIENT_ID".configProperty(),
        val clientSecret: String = "/var/run/secrets/nais.io/azuread/client_secret".readFile()
            ?: "AZURE_CLIENT_SECRET".configProperty(),
        val username: String = "USERNAME".configProperty(),
        val password: String = "PASSWORD".configProperty(),
        val tenant: String = "AZURE_TENANT".configProperty(),
        val authorityEndpoint: String = "AZURE_AUTHORITY_ENDPOINT".configProperty().removeSuffix("/")
    )
}