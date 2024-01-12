package no.nav.bidrag.cucumber.model

import no.nav.bidrag.cucumber.ABSOLUTE_CLOUD_PATH
import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.dto.CucumberTestsApi
import no.nav.bidrag.cucumber.dto.SaksbehandlerType
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream

data class CucumberTestsModel(internal val cucumberTestsApi: CucumberTestsApi) {
    companion object {
        private const val NOT_IGNORED = "not @ignored"

        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(CucumberTestsModel::class.java)

        @JvmStatic
        private val FEATURE_FILES =
            File(ABSOLUTE_CLOUD_PATH)
                .walkBottomUp()
                .filter { it.isFile }
                .filter { it.name.endsWith(".feature") }
                .toList()

        @JvmStatic
        private val NAMES_OF_FEATURE_FILES = FEATURE_FILES.joinToString(separator = ", ") { it.name }

        private fun isTagPresent(
            file: File,
            tag: String,
        ): Boolean {
            val inputStream: InputStream = file.inputStream()
            return inputStream.bufferedReader().use { it.readText() }
                .contains(tag)
        }
    }

    val ingressesForApps: List<String> get() = cucumberTestsApi.ingressesForApps
    val noContextPathForApps: List<String> get() = cucumberTestsApi.noContextPathForApps
    val sanityCheck: Boolean? get() = cucumberTestsApi.sanityCheck
    val securityToken: String? get() = cucumberTestsApi.securityToken
    val tags: List<String> get() = cucumberTestsApi.tags
    val testUsername: String? get() = cucumberTestsApi.testUsername
    val skipAuth: Boolean get() = cucumberTestsApi.skipAuth ?: false
    val saksbehandlerType: SaksbehandlerType? get() = cucumberTestsApi.medSaksbehandlerType

    constructor(
        ingressesForApps: List<String> = emptyList(),
        noContextPathForApps: List<String> = emptyList(),
        sanityCheck: Boolean? = false,
        securityToken: String? = null,
        tags: List<String> = emptyList(),
        testUsername: String? = null,
    ) : this(
        CucumberTestsApi(
            ingressesForApps = ingressesForApps,
            noContextPathForApps = noContextPathForApps,
            sanityCheck = sanityCheck,
            securityToken = securityToken,
            tags = tags,
            testUsername = testUsername,
        ),
    )

    fun getSanityCheck() = sanityCheck?.toString() ?: "false"

    fun fetchTags(): String {
        val collectTags =
            ingressesForApps
                .filter { it.contains("@tag:") }
                .map { it.split("@tag:")[1] }
                .map { "@$it" } as MutableList<String>

        collectTags.addAll(tags)
        val tagsAsStrings = transformAssertedTagsToString(collectTags)

        if (tagsAsStrings.isEmpty()) {
            throw IllegalStateException(
                "Ingen tags er oppgitt. Bruk liste med tags eller liste med ingresser som har prefiksen 'tag:' etter @",
            )
        }

        val tagsAsStringWithNotIgnored = "$tagsAsStrings and $NOT_IGNORED"
        val logValues = ArrayList(ingressesForApps)
        logValues.addAll(tags)

        LOGGER.info("Using tags - '$tagsAsStringWithNotIgnored' - from $logValues")

        return tagsAsStringWithNotIgnored
    }

    fun fetchIngress(applicationName: String): String {
        LOGGER.info("Finding ingress for '$applicationName' in $ingressesForApps")

        return ingressesForApps
            .map { it.replace("@tag:", "@") }
            .filter { it.trim().endsWith("@$applicationName") }
            .map { it.split("@")[0] }
            .first()
    }

    private fun transformAssertedTagsToString(tags: List<String>): String {
        if (tags.isEmpty()) {
            return ""
        }

        val uniqueTags = HashSet(tags).toList()

        uniqueTags.forEach { assertKnownTag(it) }

        val allTags = uniqueTags.joinToString(separator = " or ")

        return if (uniqueTags.size == 1) allTags else "($allTags)"
    }

    private fun assertKnownTag(tag: String) {
        if (isNotTagPresentInFeatureFile(tag)) {
            throw IllegalStateException("$tag er ukjent blant $NAMES_OF_FEATURE_FILES")
        }
    }

    private fun isNotTagPresentInFeatureFile(tag: String) =
        FEATURE_FILES.stream()
            .filter { file: File -> isTagPresent(file, tag) }
            .findFirst().isEmpty

    internal fun initCucumberEnvironment(): CucumberTestsModel {
        Environment.initCucumberEnvironment(this)

        return this
    }

    fun updateSecurityToken(securityToken: String?) {
        cucumberTestsApi.securityToken = securityToken
    }
}
