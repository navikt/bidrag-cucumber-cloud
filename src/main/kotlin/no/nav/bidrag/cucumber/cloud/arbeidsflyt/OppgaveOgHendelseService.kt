package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService.Assertion
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService.assertWhenNotSanityCheck
import no.nav.bidrag.cucumber.hendelse.Hendelse
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
        } else throw IllegalStateException("Antall treff: ${sokResponse.antallTreffTotalt}, men liste i response er tom!!!")
    }

    fun opprettJournalpostHendelse(hendelse: Hendelse, detaljer: Map<String, String> = emptyMap(), journalpostId: String) {
        BidragCucumberSingletons.publiserHendelse(
            JournalpostHendelse(journalpostId = journalpostId, hendelse = hendelse.name, detaljer = detaljer)
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

    fun assertThatOppgaveTilhorerEnhet(enhetsnummer: String) {
        val responseSomMap = FellesEgenskaperService.hentRestTjeneste().hentResponseSomMap()

        assertWhenNotSanityCheck(
            Assertion(
                message = "Forventet å finne oppgaven",
                value = responseSomMap["antallTreffTotalt"],
                expectation = 1,
                verify = { assertion: Assertion -> assertThat(assertion.value).`as`(assertion.message).isEqualTo(assertion.expectation) }
            ),
        )

        val tildeltEnhetsnr = (responseSomMap["oppgaver"] as List<Map<String, String?>>?)?.first()?.get("tildeltEnhetsnr")

        assertWhenNotSanityCheck(
            Assertion(
                message = "Oppgaven er tildelt enhet",
                value = tildeltEnhetsnr,
                expectation = enhetsnummer,
                verify = { assertion: Assertion -> assertThat(assertion.value).`as`(assertion.message).isEqualTo(assertion.expectation) }
            )
        )
    }
}
