package no.nav.bidrag.cucumber.model

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.cucumber.Environment
import org.slf4j.LoggerFactory

@Schema(description = "Dto med data for en testkjøring (som gjøres av `io.cucumber.core.cli.Main`)")
data class CucumberTests(
    @Schema(description = "liste med ingress@tag (ingress som brukes for en gitt nais applikasjon)") var ingressesForTags: List<String> = emptyList(),
    @Schema(description = "Om testkjøringen er en sanity check av *.feature-filer. Feiler ikke ved assertions, men kan feile ved I/O") var sanityCheck: Boolean? = false,
    @Schema(description = "Security token (on behalf of ad-token) som skal brukes ved lokal kjøring") var securityToken: String? = null,
    @Schema(description = "Brukernavn (navident/saksbehandler) for testkjøring, eks: z123456") var testUsername: String? = null
) {
    companion object {
        const val NOT_IGNORED = "not @ignored"
        private val LOGGER = LoggerFactory.getLogger(CucumberTests::class.java)
    }

    fun getSanityCheck() = sanityCheck?.toString() ?: "false"

    fun fetchIngressesForTagsAsString(): String {
        val string = ingressesForTags.joinToString(separator = ",")

        if (string.isBlank()) {
            throw IllegalStateException("ingen ingress(er) for tag(s)")
        }

        return string
    }

    fun fetchTags(): String {
        val value = fetchIngressesForTagsAsString()

        val tagstring = value.split(',')
            .joinToString(prefix = "(", postfix = " and $NOT_IGNORED)", separator = " and $NOT_IGNORED) or (") {
                it.substring(it.indexOf('@'))
            }

        LOGGER.info("Created '$tagstring' from '$value'")

        return tagstring
    }

    fun initTestEnvironment() {
        Environment(this)
    }
}
