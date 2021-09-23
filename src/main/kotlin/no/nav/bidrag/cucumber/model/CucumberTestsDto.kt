package no.nav.bidrag.cucumber.model

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.cucumber.ABSOLUTE_CLOUD_PATH
import no.nav.bidrag.cucumber.Environment
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream

@Schema(description = "Dto med data for en testkjøring (som gjøres av `io.cucumber.core.cli.Main`)")
data class CucumberTestsDto(
    @Schema(description = "liste med ingress@nais-app (kan også være en tag i en test, ingress som brukes for en gitt nais applikasjon)") var ingressesForApps: List<String> = emptyList(),
    @Schema(description = "Nais applikasjoner som ikke skal bruke applikasjonsnavnet som \"context path\" etter ingressen") var noContextPathForApps: List<String> = emptyList(),
    @Schema(description = "Om testkjøringen er en sanity check av *.feature-filer. Feiler ikke ved assertions, bare feil ved I/O") var sanityCheck: Boolean? = false,
    @Schema(description = "Security (azure) token som skal brukes ved lokal kjøring") var securityToken: String? = null,
    @Schema(description = "liste med tags som skal testes uten å oppgi ingress") var tags: List<String> = emptyList(),
    @Schema(description = "Brukernavn (test ident) for testkjøring, eks: z123456") var testUsername: String? = null
) {
    companion object {
        private const val NOT_IGNORED = "not @ignored"

        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(CucumberTestsDto::class.java)

        @JvmStatic
        private val FEATURE_FILES = File(ABSOLUTE_CLOUD_PATH)
            .walkBottomUp()
            .filter { it.isFile }
            .filter { it.name.endsWith(".feature") }
            .toList()

        @JvmStatic
        private val NAMES_OF_FEATURE_FILES = FEATURE_FILES.joinToString(separator = ", ") { it.name }

        private fun isTagPresent(file: File, tag: String): Boolean {
            val inputStream: InputStream = file.inputStream()
            return inputStream.bufferedReader().use { it.readText() }
                .contains(tag)
        }
    }

    fun getSanityCheck() = sanityCheck?.toString() ?: "false"

    fun fetchIngressesForAppsAsString(): String {
        val string = ingressesForApps.joinToString(separator = ",")

        if (string.isBlank()) {
            throw IllegalStateException("ingen ingress(er) for nais-app(s)")
        }

        return string
    }

    fun fetchTags(): String {
        val tagsFromApps = ingressesForApps
            .filterNot { it.contains("@no-tag:") }
            .map { it.split("@")[1] }
            .map { "@$it" }

        val tagsFromIngresses = transformToString(tagsFromApps)
        val stringedTags = "$tagsFromIngresses${joinWithTagList(tagsFromIngresses)}"

        val values = ArrayList(ingressesForApps)
        values.addAll(tags)

        if (stringedTags.isEmpty()) {
            throw IllegalStateException(
                "Ingen tags er oppgitt. Bruk liste med tags eller liste med ingresser som ikke har prefiksen 'no-tag:' etter @"
            )
        }

        LOGGER.info("Using tags - '$stringedTags' - from $values")

        return stringedTags
    }

    private fun transformToString(tags: List<String>): String {
        if (tags.isEmpty()) {
            return ""
        }

        tags.forEach { isFeatureTag(it) }

        return tags.joinToString(prefix = "(", postfix = " and $NOT_IGNORED)", separator = " and $NOT_IGNORED) or (")
    }

    private fun isFeatureTag(tag: String) {
        if (isTagPresentInFeatureFile(tag)) {
            throw IllegalStateException("$tag er ukjent blant $NAMES_OF_FEATURE_FILES")
        }
    }

    private fun isTagPresentInFeatureFile(tag: String) = FEATURE_FILES.stream()
        .filter { file: File -> isTagPresent(file, tag) }
        .findFirst().isEmpty

    private fun joinWithTagList(tagsFromIngresses: String): String {
        if (tags.isEmpty()) {
            return ""
        }

        val uniqueTags = tags
            .filterNot { tagsFromIngresses.contains(it) }

        if (uniqueTags.isEmpty()) {
            return ""
        }

        return if (tagsFromIngresses.isBlank()) transformToString(uniqueTags) else " or ${transformToString(uniqueTags)}"
    }

    internal fun initCucumberEnvironment() {
        Environment.initCucumberEnvironment(this)
    }

    internal fun warningLogDifferences() {
        @Suppress("NullableBooleanElvis")
        if (isNotEqual(sanityCheck ?: false, Environment.isSanityCheck)) warningForDifference("sanityCheck", sanityCheck, Environment.isSanityCheck)
        if (isNotEqual(testUsername, Environment.tenantUsername)) warningForDifference("testUsername", testUsername, Environment.testUsername)
    }

    private fun isNotEqual(dtoValue: Any?, envValue: Any?) = dtoValue != envValue

    private fun warningForDifference(name: String, property: Any?, envValue: Any?) {
        if (property == null && envValue != "null" || property != "null" && envValue == null) {
            LOGGER.warn("$property vs $envValue: (${this.javaClass.simpleName}.$name vs ${Environment::class.java.simpleName} - $name)")
        }
    }
}
