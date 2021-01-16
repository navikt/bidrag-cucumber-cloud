package no.nav.bidrag.cucumber

import com.google.gson.Gson
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import java.io.File

internal object NaisConfiguration {

    internal val CONFIG_FOR_APPLICATION: MutableMap<String, Configuration> = HashMap()
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
            CONFIG_FOR_APPLICATION[applicationName] = Configuration(envFile)
        } else {
            throw IllegalStateException("Unable to read json configuration for $applicationName")
        }

        Sikkerhet.SECURITY_FOR_APPLICATION.computeIfAbsent(applicationName) { hentSecurityForNaisApp(envFile) }

        return Sikkerhet.SECURITY_FOR_APPLICATION.getValue(applicationName)
    }

    private fun hentSecurityForNaisApp(envFile: File) = if (harAzureSomSikkerhet(envFile.parent)) Security.AZURE else Security.NONE

    private fun harAzureSomSikkerhet(naisFolder: String): Boolean {
        val naisYamlReader = File(naisFolder, "nais.yaml").bufferedReader()
        val pureYaml = mutableListOf<String>()
        naisYamlReader.useLines { lines -> lines.forEach { if (!it.contains("{{")) pureYaml.add(it) } }
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
        val configuration = CONFIG_FOR_APPLICATION[naisApplication]
            ?: throw IllegalStateException("no path for $naisApplication in $CONFIG_FOR_APPLICATION")

        val ingresses = if (configuration.endsWith(".yaml")) {
            fetchIngressesFromYaml(configuration)
        } else {
            fetchIngressesFromJson(configuration)
        }

        return fetchIngress(ingresses).replace("//", "/").replace("https:/", "https://")
    }

    private fun fetchIngressesFromYaml(configuration: Configuration): List<String> {
        val yamlReader = configuration.naisEnvironmentFile.bufferedReader()
        val yamlMap = Yaml().load<Map<String, List<String>>>(yamlReader)

        return yamlMap.getValue("ingresses")
    }

    private fun fetchIngressesFromJson(configuration: Configuration): List<String> {
        val bufferedReader = configuration.naisEnvironmentFile.bufferedReader()
        val gson = Gson()

        @Suppress("UNCHECKED_CAST")
        return (gson.fromJson(bufferedReader, Map::class.java) as Map<String, List<String>>)["ingresses"]
            ?: throw IllegalStateException("Fant ikke ingresser for ${configuration.naisEnvironmentPath}")
    }

    private fun fetchIngress(ingresses: List<String?>): String {
        for (ingress in ingresses) {
            if (ingress?.contains(Regex("dev.adeo"))!!) {
                return ingress
            }
        }

        throw IllegalStateException("Kunne ikke fastslå ingress til tjeneste!")
    }
}

data class Configuration(
    val naisEnvironmentFile: File,
    var applicationHostUrl: String = "not added"
) {
    val naisEnvironmentPath: String
        get() = naisEnvironmentFile.absolutePath

    fun endsWith(suffix: String): Boolean {
        return naisEnvironmentPath.endsWith(suffix)
    }
}
