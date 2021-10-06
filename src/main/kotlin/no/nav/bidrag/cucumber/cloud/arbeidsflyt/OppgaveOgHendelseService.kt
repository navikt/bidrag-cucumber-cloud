package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import no.nav.bidrag.cucumber.hendelse.Hendelse
import no.nav.bidrag.cucumber.model.BidragCucumberSingletons
import no.nav.bidrag.cucumber.model.JournalpostHendelse
import no.nav.bidrag.cucumber.model.PatchStatusOppgaveRequest
import no.nav.bidrag.cucumber.model.PostOppgaveRequest

/**
 * Service class in order to loosely couple logic from cucumber infrastructure
 */
object OppgaveOgHendelseService {

    fun tilbyOppgave(journalpostId: Long, tema: String) {
        val sokResponse = OppgaveConsumer.sokOppgave(journalpostId, tema)

        if (sokResponse.antallTreffTotalt == 0) {
            OppgaveConsumer.opprettOppgave(PostOppgaveRequest(journalpostId = journalpostId.toString(), tema = tema))
        } else if (sokResponse.oppgaver.isNotEmpty()) {
            val id = sokResponse.oppgaver.first().id
            val versjon = sokResponse.oppgaver.first().versjon

            OppgaveConsumer.patchOppgave(
                PatchStatusOppgaveRequest(
                    id = id,
                    status = "UNDER_BEHANDLING",
                    tema = tema,
                    versjon = versjon.toInt()
                )
            )
        }
    }

    fun opprettJournalpostHendelse(hendelse: Hendelse, detaljer: Map<String, String> = emptyMap(), journalpostId: Long) {
        BidragCucumberSingletons.publiserHendelse(
            JournalpostHendelse(journalpostId = journalpostId.toString(), detaljer = detaljer, hendelse = hendelse.name)
        )

        Thread.sleep(500) // for å gi bidrag-arbeidsflyt tid til å behandle hendelse
    }

    fun sokOppgaveForHendelse(journalpostId: Long, tema: String) {
        OppgaveConsumer.sokOppgave(journalpostId, tema)
    }

    fun ferdigstillEventuellOppgave(journalpostId: Long, tema: String) {
        val sokResponse = OppgaveConsumer.sokOppgave(journalpostId, tema)

        if (sokResponse.antallTreffTotalt > 0) {
            sokResponse.oppgaver.forEach {
                OppgaveConsumer.patchOppgave(
                    PatchStatusOppgaveRequest(
                        id = it.id,
                        status = "FERDIGSTILT",
                        tema = tema,
                        versjon = it.versjon.toInt()
                    )
                )
            }
        }
    }
}
