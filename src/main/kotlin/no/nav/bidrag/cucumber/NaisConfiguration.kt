package no.nav.bidrag.cucumber

import com.google.gson.Gson
import no.nav.bidrag.cucumber.sikkerhet.Security
import no.nav.bidrag.cucumber.sikkerhet.Sikkerhet
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import java.io.File

internal object NaisConfiguration {

    private val ENVIRONMENT_FOR_APPLICATION: MutableMap<String, EnvironmentFile> = HashMap()
    private val LOGGER = LoggerFactory.getLogger(NaisConfiguration::class.java)

    fun read(applicationName: String): Security {
        val applfolder = File("${Environment.fetchIntegrationInput().naisProjectFolder}/$applicationName")
        val naisFolder = File("${Environment.fetchIntegrationInput().naisProjectFolder}/$applicationName/nais")
        val envFile = fetchEnvFileByEnvironment(applicationName)

        LOGGER.info("> applFolder exists: ${applfolder.exists()}, path: $applfolder")
        LOGGER.info("> naisFolder exists: ${naisFolder.exists()}, path: $naisFolder")
        LOGGER.info("> envFile    exists: ${envFile.exists()}, path: $envFile")

        val canReadNaisEnvironment = applfolder.exists() && naisFolder.exists() && envFile.exists()

        if (canReadNaisEnvironment) {
            ENVIRONMENT_FOR_APPLICATION[applicationName] = EnvironmentFile(envFile)
        } else {
            throw IllegalStateException("Unable to read json configuration for $applicationName")
        }

        return Sikkerhet.fetchOrReadSecurityFor(applicationName, envFile)
    }

    internal fun hentSecurityForNaisApp(envFile: File) = if (harAzureSomSikkerhet(envFile.parent)) Security.AZURE else Security.NONE

    private fun harAzureSomSikkerhet(naisFolder: String): Boolean {
        val lines = File(naisFolder, "nais.yaml").readLines(Charsets.UTF_8)
        val pureYaml = mutableListOf<String>()
        lines.forEach { if (!it.contains("{{")) pureYaml.add(it) }
        val yamlMap = Yaml().load<Map<String, Any>>(pureYaml.joinToString("\n"))

        return isEnabled(yamlMap, mutableListOf("spec", "azure", "application", "enabled"))
    }

    private fun isEnabled(map: Map<String, Any>, keys: MutableList<String>): Boolean {
        LOGGER.info("> key=value  to use: ${keys[0]}=${map[keys[0]]}")

        if (map.containsKey(keys[0])) {
            return if (keys.size == 1) map.getValue(keys[0]) as Boolean
            else {
                @Suppress("UNCHECKED_CAST")
                val childMap = map[keys[0]] as Map<String, Any>
                keys.removeAt(0)
                isEnabled(childMap, keys)
            }
        }

        return false
    }

    private fun fetchEnvFileByEnvironment(applicationName: String): File {
        val naisProjectFolder = File(Environment.fetchIntegrationInput().naisProjectFolder).absolutePath
        val miljo = Environment.fetchIntegrationInput().environment
        val miljoYaml = File("${naisProjectFolder}/$applicationName/nais/$miljo.yaml")

        if (miljoYaml.exists()) {
            return miljoYaml
        }

        val miljoJson = File("${naisProjectFolder}/$applicationName/nais/$miljo.json")

        if (miljoJson.exists()) {
            return miljoJson
        }

        throw IllegalStateException("Unable to find $naisProjectFolder/$applicationName/nais/$miljo.? (yaml or json)")
    }

    internal fun hentApplicationHostUrl(naisApplication: String): String {
        val configuration = ENVIRONMENT_FOR_APPLICATION[naisApplication]
            ?: throw IllegalStateException("no path for $naisApplication in $ENVIRONMENT_FOR_APPLICATION")

        val ingresses = if (configuration.endsWith(".yaml")) {
            fetchIngressesFromYaml(configuration)
        } else {
            fetchIngressesFromJson(configuration)
        }

        return fetchIngress(ingresses).replace("//", "/").replace("https:/", "https://")
    }

    private fun fetchIngressesFromYaml(environmentFile: EnvironmentFile): List<String> {
        val yamlReader = environmentFile.naisEnvironmentFile.bufferedReader()
        val yamlMap = Yaml().load<Map<String, List<String>>>(yamlReader)

        return yamlMap.getValue("ingresses")
    }

    private fun fetchIngressesFromJson(environmentFile: EnvironmentFile): List<String> {
        val bufferedReader = environmentFile.naisEnvironmentFile.bufferedReader()
        val gson = Gson()

        @Suppress("UNCHECKED_CAST")
        return (gson.fromJson(bufferedReader, Map::class.java) as Map<String, List<String>>).getValue("ingresses")
    }

    private fun fetchIngress(ingresses: List<String?>): String {
        return ingresses.first { it?.contains("dev.adeo") == true } ?: throw IllegalStateException("Kunne ikke fastslå ingress til tjeneste!")
    }

    private data class EnvironmentFile(
        val naisEnvironmentFile: File
    ) {
        val naisEnvironmentPath: String
            get() = naisEnvironmentFile.absolutePath

        fun endsWith(suffix: String): Boolean {
            return naisEnvironmentPath.endsWith(suffix)
        }
    }
}
