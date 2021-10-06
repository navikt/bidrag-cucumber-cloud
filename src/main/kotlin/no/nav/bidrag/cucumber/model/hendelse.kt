package no.nav.bidrag.cucumber.model

import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.dto.HendelseApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class JournalpostHendelse(
    val journalpostId: String,
    val hendelse: String,
    val detaljer: Map<String, String?> = emptyMap()
) {
    @Suppress("unused") // mappes av jackson...
    val sporing: Sporingsdata = Sporingsdata(correlationId = CorrelationId.fetchCorrelationIdForThread(), brukerident = Environment.testUsername)

    constructor(hendelseApi: HendelseApi) : this(
        journalpostId = hendelseApi.journalpostId,
        hendelse = hendelseApi.hendelse,
        detaljer = hendelseApi.detaljer
    )
}

data class Sporingsdata(val correlationId: String, val brukerident: String? = null) {
    @Suppress("unused") // brukes av jackson (and not part of equals/hashcode for data class)
    val opprettet: LocalDateTime = LocalDateTime.now()
}

class HendelseTimeoutException(start: LocalDateTime, timeout: LocalDateTime) : RuntimeException(
    "Hendelse med timeout! Started: ${onlyTime(start)}, timed out: ${onlyTime(timeout)}"
)

private fun onlyTime(dateTime: LocalDateTime): String = dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss,SSS"))
