package no.nav.bidrag.cucumber.model

import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService
import no.nav.bidrag.cucumber.cloud.arbeidsflyt.OppgaveConsumer
import org.assertj.core.api.Assertions

class GjentaOppgaveSokRequest(
    private val antallGjentakelser: Int,
    private val journalpostId: Long,
    private val tema: String,
    private val sleepInMilleseconds: Long = 750
) {
    fun assertThatOppgaveFinnes() {
        finnOppgaveResponseMedMaksGjentakelser()
    }

    fun assertThatOppgaveHar(enhet: String?, oppgavetype: String?) {
        val responseSomMap = finnOppgaveResponseMedMaksGjentakelser()

        if (enhet != null) {
            assertTildeltEnhetsnummer(responseSomMap, enhet)
        }

        if (oppgavetype != null) {
            assertOppgavetype(responseSomMap, oppgavetype)
        }
    }

    fun assertThatDetErAntallForventedeOppgaver(antallForventet: Int) {
        val responseSomMap = finnOppgaveResponseMedMaksGjentakelser()

        assertThatOppgaveSokHarEtTotaltAntallTreff(responseSomMap, antallForventet)
    }

    private fun finnOppgaveResponseMedMaksGjentakelser(): Map<String, Any> {
        var index = 0
        var responseSomMap: Map<String, Any>

        do {
            CucumberTestRun.sleepWhenNotSanityCheck(sleepInMilleseconds)
            OppgaveConsumer.sokOppgaver(journalpostId = journalpostId, tema = tema)
            responseSomMap = FellesEgenskaperService.hentRestTjeneste().hentResponseSomMap()

            try {
                assertThatOppgaveFound(responseSomMap)
                index = antallGjentakelser
            } catch (assertionError: AssertionError) {
                if (index == antallGjentakelser && CucumberTestRun.isNotSanityCheck) {
                    throw assertionError
                }
            } finally {
                index++
            }
        } while (index < antallGjentakelser)

        return responseSomMap
    }

    private fun assertThatOppgaveFound(responseSomMap: Map<String, Any>) {
        FellesEgenskaperService.assertWhenNotSanityCheck(
            FellesEgenskaperService.Assertion(
                message = "Forventet å finne oppgaven",
                value = responseSomMap["antallTreffTotalt"],
                expectation = 1,
                verify = { assertion: FellesEgenskaperService.Assertion ->
                    Assertions.assertThat(assertion.value).`as`(assertion.message).isEqualTo(assertion.expectation)
                }
            ),
        )
    }

    private fun assertTildeltEnhetsnummer(responseSomMap: Map<String, Any>, enhet: String) {
        @Suppress("UNCHECKED_CAST") val tildeltEnhetsnr = (responseSomMap["oppgaver"] as List<Map<String, String?>>?)?.first()?.get("tildeltEnhetsnr")

        FellesEgenskaperService.assertWhenNotSanityCheck(
            FellesEgenskaperService.Assertion(
                message = "Oppgaven er tildelt enhet",
                value = tildeltEnhetsnr,
                expectation = enhet,
                verify = { assertion: FellesEgenskaperService.Assertion ->
                    Assertions.assertThat(assertion.value).`as`(assertion.message).isEqualTo(assertion.expectation)
                }
            )
        )
    }

    private fun assertOppgavetype(responseSomMap: Map<String, Any>, oppgavetype: String) {
        @Suppress("UNCHECKED_CAST") val oppgavetypeFraMap = (responseSomMap["oppgaver"] as List<Map<String, String?>>?)?.first()?.get("oppgavetype")

        FellesEgenskaperService.assertWhenNotSanityCheck(
            FellesEgenskaperService.Assertion(
                message = "Oppgaven har riktig oppgavetype",
                value = oppgavetypeFraMap,
                expectation = oppgavetype,
                verify = { assertion: FellesEgenskaperService.Assertion ->
                    Assertions.assertThat(assertion.value).`as`(assertion.message).isEqualTo(assertion.expectation)
                }
            )
        )
    }

    private fun assertThatOppgaveSokHarEtTotaltAntallTreff(responseSomMap: Map<String, Any>, antallForventet: Int) {
        FellesEgenskaperService.assertWhenNotSanityCheck(
            FellesEgenskaperService.Assertion(
                message = "Forventet å finne oppgaver",
                value = responseSomMap["antallTreffTotalt"],
                expectation = antallForventet,
                verify = { assertion: FellesEgenskaperService.Assertion ->
                    Assertions.assertThat(assertion.value).`as`(assertion.message).isEqualTo(assertion.expectation)
                }
            ),
        )
    }
}
