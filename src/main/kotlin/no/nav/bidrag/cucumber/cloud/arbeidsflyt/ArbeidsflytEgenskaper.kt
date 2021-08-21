package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService.Assertion
import no.nav.bidrag.cucumber.cloud.arbeidsflyt.ArbeidsflytEgenskaperEndreFagomradeService.opprettJournalpostHendelse
import no.nav.bidrag.cucumber.cloud.arbeidsflyt.ArbeidsflytEgenskaperEndreFagomradeService.opprettOppgave
import no.nav.bidrag.cucumber.cloud.arbeidsflyt.ArbeidsflytEgenskaperEndreFagomradeService.sokOppgaveForHendelse
import org.assertj.core.api.Assertions.assertThat

class ArbeidsflytEgenskaper : No {

    companion object {
        internal val prefiksetJournalpostIdForHendelse = PrefiksetJournalpostIdForHendelse()
        fun hentId(hendelse: Hendelse, tema: String) = prefiksetJournalpostIdForHendelse.hent(hendelse, tema)
    }

    init {
        Og("at en oppgave opprettes for {string} med journalpostId {long} og tema {string}") { hendelse: String, journalpostId: Long, tema: String ->
            opprettOppgave(Hendelse.valueOf(hendelse), journalpostId, tema)
        }

        Når("det opprettes en journalposthendelse - {string} - for endring av fagområde fra {string} til {string}") { hendelse: String, fraFagomrade: String, tilFagomrade: String ->
            opprettJournalpostHendelse(Hendelse.valueOf(hendelse), mapOf("fagomrade" to tilFagomrade), fraFagomrade)
        }


        Når("jeg søker etter oppgave opprettet for {string} på tema {string}") { hendelse: String, tema: String ->
            sokOppgaveForHendelse(Hendelse.valueOf(hendelse), tema)
        }

        Så("skal jeg finne oppgaven i søkeresultatet") {
            FellesEgenskaperService.assertWhenNotSanityCheck(
                Assertion(
                    message = "Søkeresultatet skal være 1",
                    value = FellesEgenskaperService.hentRestTjeneste().hentResponseSomMap()["antallTreff"],
                    expectation = 1
                ),
                this::harForventetAntallTreff
            )
        }

        Så("skal jeg ikke finne oppgaven i søkeresultatet") {
            FellesEgenskaperService.assertWhenNotSanityCheck(
                Assertion(
                    "Søkeresultatet skal være 0",
                    FellesEgenskaperService.hentRestTjeneste().hentResponseSomMap()["antallTreff"],
                    0
                ),
                this::harForventetAntallTreff
            )
        }
    }

    private fun harForventetAntallTreff(assertion: Assertion) {
        assertThat(assertion.expectation).`as`(assertion.message).isEqualTo(1)
    }
}
