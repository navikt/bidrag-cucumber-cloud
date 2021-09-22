package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.cloud.arbeidsflyt.JournalpostIdForOppgave.Hendelse
import no.nav.bidrag.cucumber.hendelse.JournalpostHendelse
import no.nav.bidrag.cucumber.model.BidragCucumberSingletons
import org.slf4j.LoggerFactory

/**
 * Service class in order to loosely couple logic from cucumber infrastructure
 */
object ArbeidsflytEgenskaperEndreFagomradeService {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(ArbeidsflytEgenskaperEndreFagomradeService::class.java)

    fun opprettOppgaveNarUkjent(hendelse: String, journalpostId: Long, tema: String) {
        JournalpostIdForOppgave.leggTil(Hendelse.valueOf(hendelse), journalpostId, tema)
        val sokResponse = OppgaveConsumer.sokOppgave(journalpostId, tema)

        if (sokResponse.antallTreffTotalt == 0) {
            OppgaveConsumer.opprettOppgave(journalpostId, tema)
        } else if (sokResponse.oppgaver.isNotEmpty()) {
            val id = sokResponse.oppgaver.first().id
            val versjon = sokResponse.oppgaver.first().versjon

            OppgaveConsumer.settOppgaveTilUnderBehandling(id, tema, versjon)
        }
    }

    fun opprettJournalpostHendelse(hendelse: Hendelse, detaljer: Map<String, String>, tema: String) {
        val journalpostHendelse = JournalpostHendelse(detaljer, hendelse, tema)

        BidragCucumberSingletons.hendelseProducer?.publish(journalpostHendelse)
            ?: LOGGER.info(
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
        val journalpostId = JournalpostIdForOppgave.hentJournalpostId(hendelse, tema)
        OppgaveConsumer.sokOppgave(journalpostId, tema)
    }
}
