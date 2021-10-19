package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import no.nav.bidrag.cucumber.FAGOMRADE_BIDRAG
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService.Assertion
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService.assertWhenNotSanityCheck
import no.nav.bidrag.cucumber.model.BidragCucumberSingletons
import no.nav.bidrag.cucumber.model.JournalpostHendelse
import no.nav.bidrag.cucumber.model.PatchStatusOppgaveRequest
import no.nav.bidrag.cucumber.model.PostOppgaveRequest
import org.assertj.core.api.Assertions.assertThat

/**
 * Service class in order to loosely couple logic from cucumber infrastructure
 */
@Suppress("UNCHECKED_CAST")
object OppgaveOgHendelseService {

    fun tilbyOppgave(journalpostHendelse: JournalpostHendelse, oppgavetype: String? = null) {
        val sokResponse = OppgaveConsumer.sokOppgave(journalpostHendelse.hentJournalpostIdUtenPrefix(), journalpostHendelse.fagomrade!!)
        val fagomrade: String = journalpostHendelse.fagomrade ?: FAGOMRADE_BIDRAG
        val enhetsnummer: String = journalpostHendelse.enhet ?: "4806"

        if (sokResponse.antallTreffTotalt == 0) {
            OppgaveConsumer.opprettOppgave(
                PostOppgaveRequest(
                    journalpostId = journalpostHendelse.hentJournalpostIdStrengUtenPrefix(),
                    tema = fagomrade,
                    tildeltEnhetsnr = enhetsnummer,
                    oppgavetype = oppgavetype ?: "BEH_SAK"
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
                        versjon = it.versjon.toInt(),
                        tildeltEnhetsnr = it.tildeltEnhetsnr
                    )
                )
            }
        }
    }

    fun assertThatOppgaveTilhorer(enhet: String? = null, oppgavetype: String? = null) {
        val responseSomMap = FellesEgenskaperService.hentRestTjeneste().hentResponseSomMap()

        assertWhenNotSanityCheck(
            Assertion(
                message = "Forventet å finne oppgaven",
                value = responseSomMap["antallTreffTotalt"],
                expectation = 1,
                verify = { assertion: Assertion -> assertThat(assertion.value).`as`(assertion.message).isEqualTo(assertion.expectation) }
            ),
        )

        if (enhet != null) {
            assertTildeltEnhetsnummer(responseSomMap, enhet)
        }

        if (oppgavetype != null) {
            assertOppgavetype(responseSomMap, oppgavetype)
        }
    }

    private fun assertTildeltEnhetsnummer(responseSomMap: Map<String, Any>, enhet: String) {
        val tildeltEnhetsnr = (responseSomMap["oppgaver"] as List<Map<String, String?>>?)?.first()?.get("tildeltEnhetsnr")

        assertWhenNotSanityCheck(
            Assertion(
                message = "Oppgaven er tildelt enhet",
                value = tildeltEnhetsnr,
                expectation = enhet,
                verify = { assertion: Assertion -> assertThat(assertion.value).`as`(assertion.message).isEqualTo(assertion.expectation) }
            )
        )
    }

    private fun assertOppgavetype(responseSomMap: Map<String, Any>, oppgavetype: String) {
        val oppgavetypeFraMap = (responseSomMap["oppgaver"] as List<Map<String, String?>>?)?.first()?.get("oppgavetype")

        assertWhenNotSanityCheck(
            Assertion(
                message = "Oppgaven har riktig oppgavetype",
                value = oppgavetypeFraMap,
                expectation = oppgavetype,
                verify = { assertion: Assertion -> assertThat(assertion.value).`as`(assertion.message).isEqualTo(assertion.expectation) }
            )
        )
    }
}
