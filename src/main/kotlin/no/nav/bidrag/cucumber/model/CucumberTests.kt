package no.nav.bidrag.cucumber.model

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.cucumber.Environment
import org.slf4j.LoggerFactory

@Schema(description = "Dto med data for en testkjøring (som gjøres av `io.cucumber.core.cli.Main`)")
data class CucumberTests(
    @Schema(description = "liste med ingress@nais-app (kan også være en tag i en test, ingress som brukes for en gitt nais applikasjon)") var ingressesForApps: List<String> = emptyList(),
    @Schema(description = "Om testkjøringen er en sanity check av *.feature-filer. Feiler ikke ved assertions, bare feil ved I/O") var sanityCheck: Boolean? = false,
    @Schema(description = "Security token (on behalf of ad-token) som skal brukes ved lokal kjøring") var securityToken: String? = null,
    @Schema(description = "liste med tags som skal testes uten å oppgi ingress") var tags: List<String> = emptyList(),
    @Schema(description = "Brukernavn (navident/saksbehandler) for testkjøring, eks: z123456") var testUsername: String? = null
) {
    companion object {
        const val NOT_IGNORED = "not @ignored"
        private val LOGGER = LoggerFactory.getLogger(CucumberTests::class.java)
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
            .filter { it.contains("@tag:") }
            .map { it.substring(it.indexOf('@') + 5) }
            .map { "@${it}" }

        val tagsFromIngresses = transformToString(tagsFromApps)
        val stringedTags = "$tagsFromIngresses${joinWithTagList(tagsFromIngresses)}"

        val values = ArrayList(ingressesForApps)
        values.addAll(tags)
        LOGGER.info("Created '$stringedTags' from $values")

        return stringedTags
    }

    fun transformToString(tags: List<String>): String {
        if (tags.isEmpty()) {
            return ""
        }

        return tags.joinToString(prefix = "(", postfix = " and $NOT_IGNORED)", separator = " and $NOT_IGNORED) or (")
    }

    private fun joinWithTagList(tagsFromIngresses: String): String {
        if (tags.isEmpty()) {
            return ""
        }

        return if (tagsFromIngresses.isBlank()) transformToString(tags) else " or ${transformToString(tags)}"
    }

    internal fun initCucumberEnvironment() {
        Environment.resetCucumberEnvironment()
        Environment.initCucumberEnvironment(this)
    }
}
