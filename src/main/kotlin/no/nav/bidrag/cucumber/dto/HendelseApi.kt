package no.nav.bidrag.cucumber.dto

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.bidrag.transport.dokument.JournalpostStatus

@Schema(description = "En intern hendelse for endring av journalpost")
data class HendelseApi(
    @Schema(description = "Hendelsen oppstod på journalpostId") val journalpostId: String = "na",
    @Schema(description = "Eventuell aktør id på hendelsen") var aktorId: String? = null,
    @Schema(description = "Brukerident som kan brukes til sporing av hendelsen") val brukerident: String? = null,
    @Schema(description = "Fagområdet som journalposten tilhører") var fagomrade: String? = null,
    @Schema(description = "Enheten som journalposten tilhører") var enhet: String? = null,
    @Schema(description = "Journalposten journalstatus") var journalstatus: JournalpostStatus? = null
)
