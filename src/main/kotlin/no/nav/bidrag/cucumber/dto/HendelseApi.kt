package no.nav.bidrag.cucumber.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "En intern hendelse for behandling av journalpost")
data class HendelseApi(
    @Schema(description = "Hendelsen oppstod på journalpostId") val journalpostId: String = "na",
    @Schema(description = "Hendelsen som oppstod, ex: AVVIK_ENDRE_FAGOMRADE") val hendelse: String = "na",
    @Schema(description = "Brukerident som kan brukes til sporing av hendelsen") val brukerident: String? = null,
    @Schema(description = "Detaljer for hendelsen") val detaljer: Map<String, String?> = emptyMap()
)
