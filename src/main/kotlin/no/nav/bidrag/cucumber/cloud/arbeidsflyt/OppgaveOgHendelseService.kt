package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import no.nav.bidrag.cucumber.FAGOMRADE_BIDRAG
import no.nav.bidrag.cucumber.model.BidragCucumberSingletons
import no.nav.bidrag.cucumber.model.CucumberTestRun
import no.nav.bidrag.cucumber.model.GjentaOppgaveSokRequest
import no.nav.bidrag.cucumber.model.JournalpostHendelse
import no.nav.bidrag.cucumber.model.PatchStatusOppgaveRequest
import no.nav.bidrag.cucumber.model.PostOppgaveRequest

/**
 * Service class in order to loosely couple logic from cucumber infrastructure
 */
@Suppress("UNCHECKED_CAST")
object OppgaveOgHendelseService {

    @JvmStatic
    internal val GJENTA_OPPGAVE_SOK_REQUEST = ThreadLocal<GjentaOppgaveSokRequest>()

    fun tilbyOppgave(journalpostHendelse: JournalpostHendelse, oppgavetype: String? = null) {
        val sokResponse = OppgaveConsumer.sokOppgaver(journalpostHendelse.hentJournalpostIdUtenPrefix(), journalpostHendelse.fagomrade!!)
        val fagomrade: String = journalpostHendelse.fagomrade ?: FAGOMRADE_BIDRAG
        val enhetsnummer: String = journalpostHendelse.enhet ?: "4806"

        if (sokResponse.antallTreffTotalt == 0) {
            OppgaveConsumer.opprettOppgave(
                PostOppgaveRequest(
                    journalpostId = journalpostHendelse.hentJournalpostIdStrengUtenPrefix(),
                    tema = fagomrade,
                    tildeltEnhetsnr = enhetsnummer,
                    oppgavetype = oppgavetype ?: "JFR"
                )
            )
        } else if (sokResponse.oppgaver.isNotEmpty()) {
            val id = sokResponse.oppgaver.first().id
            val versjon = sokResponse.oppgaver.first().versjon

            OppgaveConsumer.patchOppgave(
                PatchStatusOppgaveRequest(
                    id = id,
                    status = "UNDER_BEHANDLING",
                    tema = fagomrade,
                    versjon = versjon.toInt(),
                    tildeltEnhetsnr = enhetsnummer,
                    oppgavetype = oppgavetype
                )
            )
        } else throw IllegalStateException("Antall treff: ${sokResponse.antallTreffTotalt}, men liste i response er tom!!!")
    }

    fun opprettJournalpostHendelse(journalpostHendelse: JournalpostHendelse) {
        BidragCucumberSingletons.publiserHendelse(journalpostHendelse = journalpostHendelse)
    }

    fun sokOppgaverEtterBehandlingAvHendelse(hendelse: JournalpostHendelse, tema: String, sleepInMilliseconds: Long) {
        CucumberTestRun.sleepWhenNotSanityCheck(sleepInMilliseconds)
        OppgaveConsumer.sokOppgaver(journalpostId = hendelse.hentJournalpostIdUtenPrefix(), tema = tema)
    }

    fun sokOpprettetOppgaveForHendelse(journalpostId: Long, tema: String, antallGjentakelser: Int, sleepInMilleseconds: Long = 1500) {
        GJENTA_OPPGAVE_SOK_REQUEST.set(
            GjentaOppgaveSokRequest(
                antallGjentakelser = antallGjentakelser,
                journalpostId = journalpostId,
                tema = tema,
                sleepInMilleseconds = sleepInMilleseconds
            )
        )
    }

    fun ferdigstillEventuellOppgave(journalpostIdMedPrefix: String, journalpostId: Long, tema: String) {
        val sokResponse = OppgaveConsumer.sokOppgaver(journalpostId, tema)

        if (sokResponse.antallTreffTotalt > 0) {
            sokResponse.oppgaver.forEach {
                OppgaveConsumer.patchOppgave(
                    PatchStatusOppgaveRequest(
                        id = it.id,
                        status = "FERDIGSTILT",
                        tema = tema,
                        versjon = it.versjon.toInt(),
                        tildeltEnhetsnr = it.tildeltEnhetsnr
                    )
                )
                opprettJournalpostHendelse(
                    JournalpostHendelse(
                        journalpostId = journalpostIdMedPrefix,
                        journalstatus = "J",
                        fagomrade= "BID",
                        enhet = "4806"
                    ))
            }
        }

        // Wait for hendelse to finish
        Thread.sleep(2000)
    }

    fun assertThatOppgaveHar(enhet: String? = null, oppgavetype: String? = null) {
        GJENTA_OPPGAVE_SOK_REQUEST.get().assertThatOppgaveHar(enhet, oppgavetype)
    }

    fun assertThatOPpgaveFinnes() {
        GJENTA_OPPGAVE_SOK_REQUEST.get().assertThatOppgaveFinnes()
    }

    fun assertThatDetErTotaltEnOppgaveFraSokeresultat(antallForventet: Int) {
        GJENTA_OPPGAVE_SOK_REQUEST.get().assertThatDetErAntallForventedeOppgaver(antallForventet)
    }
}
