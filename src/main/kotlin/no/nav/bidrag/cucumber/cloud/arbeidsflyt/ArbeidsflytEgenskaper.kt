package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService.Assertion
import no.nav.bidrag.cucumber.cloud.arbeidsflyt.ArbeidsflytEgenskaperService.opprettJournalpostHendelse
import no.nav.bidrag.cucumber.cloud.arbeidsflyt.ArbeidsflytEgenskaperService.opprettOppgave
import no.nav.bidrag.cucumber.cloud.arbeidsflyt.ArbeidsflytEgenskaperService.sokOppgave
import org.assertj.core.api.Assertions.assertThat

class ArbeidsflytEgenskaper : No {

    init {
        Og("at en oppgave opprettes med journalpostId {long} og tema {string}") { journalpostId: Long, tema: String ->
            opprettOppgave(journalpostId, tema)
        }

        Når("det opprettes en journalposthendelse - {string} - for endring av fagområde til {string}") { hendelse: String, tilFagomrade: String ->
            opprettJournalpostHendelse(hendelse, mapOf("fagomrade" to tilFagomrade))
        }


        Når("jeg søker etter oppgave") { sokOppgave() }

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
