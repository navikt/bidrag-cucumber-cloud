package no.nav.bidrag.cucumber.model

import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService
import no.nav.bidrag.cucumber.cloud.arbeidsflyt.OppgaveConsumer
import org.assertj.core.api.Assertions.assertThat

class GjentaOppgaveSokRequest(
    private val antallGjentakelser: Int,
    private val journalpostId: Long,
    private val tema: String,
    private val sleepInMilleseconds: Long = 750
) {
    fun assertThatOppgaveFinnes() {
        finnOppgaveResponseMedMaksGjentakelser()
    }

    fun assertThatOppgaveHar(enhet: String?, oppgavetype: String?, aktorId: String?) {
        val responseSomMap = finnOppgaveResponseMedMaksGjentakelser()

        if (enhet != null) {
            assertTildeltEnhetsnummer(responseSomMap, enhet)
        }

        if (oppgavetype != null) {
            assertOppgavetype(responseSomMap, oppgavetype)
        }

        if (aktorId != null) {
            assertAktorid(responseSomMap, aktorId)
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
            responseSomMap = CucumberTestRun.hentRestTjenesteTilTesting().hentResponseSomMap()

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
            Assertion(
                message = "Forventet å finne oppgaven",
                value = responseSomMap["antallTreffTotalt"],
                expectation = 1
            ) { assertThat(it.value).`as`(it.message).isEqualTo(it.expectation) }
        )
    }

    private fun assertTildeltEnhetsnummer(responseSomMap: Map<String, Any>, enhet: String) {
        @Suppress("UNCHECKED_CAST")
        val tildeltEnhetsnr = (responseSomMap["oppgaver"] as List<Map<String, String?>>?)?.first()?.get("tildeltEnhetsnr")

        FellesEgenskaperService.assertWhenNotSanityCheck(
            Assertion(
                message = "Oppgaven er tildelt enhet",
                value = tildeltEnhetsnr,
                expectation = enhet
            ) { assertThat(it.value).`as`(it.message).isEqualTo(it.expectation) }
        )
    }

    private fun assertAktorid(responseSomMap: Map<String, Any>, aktoerId: String) {
        @Suppress("UNCHECKED_CAST")
        val oppgavetypeFraMap = (responseSomMap["oppgaver"] as List<Map<String, String?>>?)?.first()?.get("aktoerId")

        FellesEgenskaperService.assertWhenNotSanityCheck(
            Assertion(
                message = "Oppgaven har riktig aktoerId",
                value = oppgavetypeFraMap,
                expectation = aktoerId
            ) { assertThat(it.value).`as`(it.message).isEqualTo(it.expectation) }
        )
    }

    private fun assertOppgavetype(responseSomMap: Map<String, Any>, oppgavetype: String) {
        @Suppress("UNCHECKED_CAST")
        val oppgavetypeFraMap = (responseSomMap["oppgaver"] as List<Map<String, String?>>?)?.first()?.get("oppgavetype")

        FellesEgenskaperService.assertWhenNotSanityCheck(
            Assertion(
                message = "Oppgaven har riktig oppgavetype",
                value = oppgavetypeFraMap,
                expectation = oppgavetype
            ) { assertThat(it.value).`as`(it.message).isEqualTo(it.expectation) }
        )
    }

    private fun assertThatOppgaveSokHarEtTotaltAntallTreff(responseSomMap: Map<String, Any>, antallForventet: Int) {
        FellesEgenskaperService.assertWhenNotSanityCheck(
            Assertion(
                message = "Forventet å finne oppgaver",
                value = responseSomMap["antallTreffTotalt"],
                expectation = antallForventet
            ) { assertThat(it.value).`as`(it.message).isEqualTo(it.expectation) }
        )
    }
}
