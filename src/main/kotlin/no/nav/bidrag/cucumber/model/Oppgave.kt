package no.nav.bidrag.cucumber.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate

data class OppgaveSokResponse(var antallTreffTotalt: Int = 0, var oppgaver: List<Oppgave> = emptyList())
data class Oppgave(var id: Long = -1, var versjon: String = "na", var tildeltEnhetsnr: String = "na", val oppgavetype: String? = null)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PatchStatusOppgaveRequest(
    override var id: Long,
    var status: String,
    var tema: String,
    var versjon: Int,
    var tildeltEnhetsnr: String,
    var oppgavetype: String? = null
) : MedOppgaveId

data class PostOppgaveRequest(
    var journalpostId: String,
    var tema: String,
    var oppgavetype: String = "JFR",
    var prioritet: String = "HOY",
    var aktivDato: LocalDate = LocalDate.now().minusDays(1),
    var tildeltEnhetsnr: String = "1001"
)

interface MedOppgaveId {
    var id: Long
}
