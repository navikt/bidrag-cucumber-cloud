package no.nav.bidrag.cucumber

import com.google.gson.Gson
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

private val LOGGER = LoggerFactory.getLogger(NaisConfiguration::class.java)

internal class NaisConfiguration {
    companion object {
        private val namespaceJsonFilePathPerAppName: MutableMap<String, String> = HashMap()
    }

    fun read(applicationName: String) {
        val applfolder = File("${Environment.naisProjectFolder}/$applicationName")
        val naisFolder = File("${Environment.naisProjectFolder}/$applicationName/nais")
        val jsonFile = fetchJsonByEnvironmentOrNamespace(applicationName)

        LOGGER.info("> applFolder exists: ${applfolder.exists()}, path: $applfolder")
        LOGGER.info("> naisFolder exists: ${naisFolder.exists()}, path: $naisFolder")
        LOGGER.info("> jsonFile   exists: ${jsonFile.exists()}, path: $jsonFile")

        val canReadNaisJson = applfolder.exists() && naisFolder.exists() && jsonFile.exists()

        if (canReadNaisJson) {
            namespaceJsonFilePathPerAppName[applicationName] = jsonFile.absolutePath
        } else {
            throw IllegalStateException("Unable to read json configuration for $applicationName")
        }
    }

    private fun fetchJsonByEnvironmentOrNamespace(applicationName: String): File {
        val miljoJson = File("${Environment.naisProjectFolder}/$applicationName/nais/${Environment.miljo}.json")

        if (miljoJson.exists()) {
            return miljoJson
        } else {
            LOGGER.warn("Unable to find ${Environment.miljo}.json, using ${Environment.namespace}.json")
        }

        return File("${Environment.naisProjectFolder}/$applicationName/nais/${Environment.namespace}.json")
    }

    internal fun hentApplicationHostUrl(naisApplication: String): String {
        val nameSpaceJsonFile = namespaceJsonFilePathPerAppName[naisApplication]
                ?: throw IllegalStateException("no path for $naisApplication in $namespaceJsonFilePathPerAppName")

        val jsonFileAsMap = readWithGson(nameSpaceJsonFile)

        for (json in jsonFileAsMap) {
            println(json)
        }

        @Suppress("UNCHECKED_CAST") val ingresses = jsonFileAsMap["ingresses"] as List<String>
        return fetchIngress(ingresses).replace("//", "/").replace("https:/", "https://")
    }

    private fun fetchIngress(ingresses: List<String?>): String {
        for (ingress in ingresses) {
            if (ingress?.contains(Regex("dev.adeo"))!!) {
                return ingress
            }
        }

        throw IllegalStateException("Kunne ikke fastslå ingress til tjeneste!")
    }

    private fun readWithGson(jsonPath: String): Map<String, Any> {
        val bufferedReader = BufferedReader(FileReader(jsonPath))
        val gson = Gson()

        @Suppress("UNCHECKED_CAST")
        return gson.fromJson(bufferedReader, Map::class.java) as Map<String, Any>
    }
}
