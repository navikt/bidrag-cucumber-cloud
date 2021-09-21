package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.ScenarioManager
import no.nav.bidrag.cucumber.cloud.arbeidsflyt.PrefiksetJournalpostIdForHendelse.Hendelse
import no.nav.bidrag.cucumber.hendelse.JournalpostHendelse
import no.nav.bidrag.cucumber.model.BidragCucumberSingletons

/**
 * Service class in order to loosely couple logic from cucumber infrastructure
 */
object ArbeidsflytEgenskaperEndreFagomradeService {

    fun opprettOppgaveNarUkjent(prefiksJournalpostId: String, tema: String) {
        val sokResponse = OppgaveConsumer.sokOppgave(prefiksJournalpostId, tema)

        if (sokResponse == null || sokResponse.antallTreffTotalt == 0) {
            OppgaveConsumer.opprettOppgave(prefiksJournalpostId, tema)
        } else if (sokResponse.oppgaver.isNotEmpty()) {
            val id = sokResponse.oppgaver.first().id
            val versjon = sokResponse.oppgaver.first().versjon

            OppgaveConsumer.settOppgaveTilUnderBehandling(id, tema, versjon)
        }
    }

    fun opprettJournalpostHendelse(hendelse: Hendelse, detaljer: Map<String, String>, tema: String) {
        val journalpostHendelse = JournalpostHendelse(detaljer, hendelse, tema)

        BidragCucumberSingletons.hendelseProducer?.publish(journalpostHendelse)
            ?: ScenarioManager.log(
                "Cannot publish $hendelse ${
                    if (Environment.isSanityCheck) {
                        "while running sanity check!"
                    } else {
                        "when spring context is not initialized!"
                    }
                }"
            )

        Thread.sleep(500) // for å sørge for at kafka melding blir behandlet i bidrag-arbeidsflyt
    }

    fun sokOppgaveForHendelse(hendelse: Hendelse, tema: String) {
        val journalpostId = ArbeidsflytEgenskaper.prefiksetJournalpostIdForHendelse.hent(hendelse, tema)
        OppgaveConsumer.sokOppgave(journalpostId, tema)
    }
}
