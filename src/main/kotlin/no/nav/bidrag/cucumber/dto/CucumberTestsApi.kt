package no.nav.bidrag.cucumber.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Dto med data for en testkjøring (som gjøres av `io.cucumber.core.cli.Main`)")
data class CucumberTestsApi(
    @Schema(description = "liste med ingress@nais-app (kan også være en tag i en test, ingress som brukes for en gitt nais applikasjon)") var ingressesForApps: List<String> = emptyList(),
    @Schema(description = "Nais applikasjoner som ikke skal bruke applikasjonsnavnet som \"context path\" etter ingressen") var noContextPathForApps: List<String> = emptyList(),
    @Schema(description = "Om testkjøringen er en sanity check av *.feature-filer. Feiler ikke ved assertions, bare feil ved I/O") var sanityCheck: Boolean? = false,
    @Schema(description = "Security (azure) token som skal brukes ved lokal kjøring") var securityToken: String? = null,
    @Schema(description = "liste med tags som skal testes uten å oppgi ingress") var tags: List<String> = emptyList(),
    @Schema(description = "Brukernavn (test ident) for testkjøring, eks: z123456") var testUsername: String? = null,
    @Schema(description = "Om autentisering skal brukes ved kall mot API") var skipAuth: Boolean? = false
)
