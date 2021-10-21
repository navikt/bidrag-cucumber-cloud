package no.nav.bidrag.cucumber.model

import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.dto.HendelseApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class JournalpostHendelse(
    var journalpostId: String = "na",
    var aktorId: String? = null,
    var fagomrade: String? = null,
    var enhet: String? = null,
    var journalstatus: String? = null,
) {
    @Suppress("unused") // brukes for sporing av data som publiseres
    var sporing: Sporingsdata = Sporingsdata(brukerident = Environment.testUsername)

    constructor(hendelseApi: HendelseApi) : this(
        journalpostId = hendelseApi.journalpostId,
        aktorId = hendelseApi.aktorId,
        fagomrade = hendelseApi.fagomrade,
        enhet = hendelseApi.enhet,
        journalstatus = hendelseApi.journalstatus,
    )

    internal fun hentJournalpostIdUtenPrefix() = hentJournalpostIdStrengUtenPrefix().toLong()
    fun hentJournalpostIdStrengUtenPrefix() = journalpostId.split('-')[1]
}

data class Sporingsdata(
    var brukerident: String? = null
) {
    var correlationId: String = System.currentTimeMillis().toString()

    init {
        val fromThread = CorrelationId.fetchCorrelationIdForThread()

        if (fromThread != null) {
            correlationId = fromThread
        }
    }
}

class HendelseTimeoutException(start: LocalDateTime, timeout: LocalDateTime) : RuntimeException(
    "Hendelse med timeout! Started: ${onlyTime(start)}, timed out: ${onlyTime(timeout)}"
)

private fun onlyTime(dateTime: LocalDateTime): String = dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss,SSS"))
