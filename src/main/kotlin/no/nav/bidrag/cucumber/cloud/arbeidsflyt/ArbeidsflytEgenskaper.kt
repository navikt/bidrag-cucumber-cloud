package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService.Assertion
import no.nav.bidrag.cucumber.hendelse.Hendelse
import org.assertj.core.api.Assertions.assertThat

@Suppress("unused") // cucumber
class ArbeidsflytEgenskaper : No {

    init {
        Og("at en oppgave opprettes for {string} med journalpostId {long} og tema {string}") { hendelse: String, journalpostId: Long, tema: String ->
            OppgaveOgHendelseService.tilbyOppgave(hendelse, journalpostId, tema)
        }

        Når("det opprettes en journalposthendelse - {string} - for endring av fagområde fra {string} til {string}") { hendelseStreng: String, fraFagomrade: String, tilFagomrade: String ->
            val hendelse = Hendelse.valueOf(hendelseStreng)
            OppgaveOgHendelseService.opprettJournalpostHendelse(hendelse, mapOf("fagomrade" to tilFagomrade), fraFagomrade)
        }

        Når("jeg søker etter oppgave opprettet for {string} på tema {string}") { hendelse: String, tema: String ->
            OppgaveOgHendelseService.sokOppgaveForHendelse(Hendelse.valueOf(hendelse), tema)
        }

        Så("skal jeg finne oppgaven i søkeresultatet") {
            FellesEgenskaperService.assertWhenNotSanityCheck(
                Assertion(
                    message = "Forventet å finne oppgaven",
                    value = FellesEgenskaperService.hentRestTjeneste().hentResponseSomMap()["antallTreffTotalt"],
                    expectation = 1,
                    verify = this::harForventetAntallTreff
                ),
            )
        }

        Så("skal jeg ikke finne oppgaven i søkeresultatet") {
            FellesEgenskaperService.assertWhenNotSanityCheck(
                Assertion(
                    message = "Forventet ikke å finne oppgaven",
                    value = FellesEgenskaperService.hentRestTjeneste().hentResponseSomMap()["antallTreffTotalt"],
                    expectation = 0,
                    verify = this::harForventetAntallTreff
                ),
            )
        }
    }

    private fun harForventetAntallTreff(assertion: Assertion) {
        assertThat(assertion.value).`as`(assertion.message).isEqualTo(assertion.expectation)
    }
}
