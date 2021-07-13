package no.nav.bidrag.cucumber.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Dto med data for hvilke data en testkjøring (av `io.cucumber.core.cli.Main`) skal bruke")
data class CucumberTests(
    @Schema(description = "liste med ingress@tag (ingress som brukes for en gitt nais applikasjon)") var ingressesForTags: List<String> = emptyList(),
    @Schema(description = "Om testkjøringen er en sanity check av *.feature-filer. Feiler ikke ved assertions") var sanityCheck: Boolean? = false,
    @Schema(description = "Security token (on behalf of ad-token) som skal brukes ved lokal kjøring") var securityToken: String? = null,
    @Schema(description = "Brukernavn (navident/saksbehandler) for testkjøring, eks: z123456") var testUsername: String? = null
) {
    fun getSanityCheck() = sanityCheck?.toString() ?: "false"
    fun hasTestUsername() = testUsername != null
    fun ingressesForTagsAsString(): String {
        val string = ingressesForTags.joinToString(separator = ",")

        if (string.isBlank()) {
            throw IllegalStateException("ingen ingress(er) for tag(s)")
        }

        return string
    }
}
