package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import no.nav.bidrag.cucumber.model.BidragCucumberSingletons
import no.nav.bidrag.cucumber.model.CucumberTestRun
import no.nav.bidrag.cucumber.model.CucumberTestRun.Companion.hentRestTjenesteTilTesting
import no.nav.bidrag.cucumber.model.MedOppgaveId
import no.nav.bidrag.cucumber.model.OppgaveSokResponse
import org.slf4j.LoggerFactory

object OppgaveConsumer {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(OppgaveConsumer::class.java)

    fun opprettOppgave(oppgave: Any) {
        LOGGER.info("oppretter oppgave: $oppgave")

        hentRestTjenesteTilTesting().exchangePost("/api/v1/oppgaver", oppgave)
    }

    fun sokOppgaver(
        journalpostId: Long,
        tema: String,
    ): OppgaveSokResponse {
        hentRestTjenesteTilTesting().exchangeGet(
            "/api/v1/oppgaver?journalpostId=$journalpostId&journalpostId=$tema-$journalpostId&statuskategori=AAPEN&tema=$tema",
        )

        try {
            val response = hentRestTjenesteTilTesting().hentResponse() ?: return OppgaveSokResponse()

            return if (CucumberTestRun.isSanityCheck) {
                OppgaveSokResponse()
            } else {
                BidragCucumberSingletons.readValue(response, OppgaveSokResponse::class.java)
            }
        } finally {
            val oppgaveSokResponse =
                if (hentRestTjenesteTilTesting().responseEntity != null) {
                    "Har OppgaveSokResponse (${hentRestTjenesteTilTesting().hentResponse()})"
                } else {
                    "Mangler OppgaveSokResponse ${if (CucumberTestRun.isSanityCheck) "og det er" else "og det er ikke"} sanity check!"
                }

            LOGGER.info("$oppgaveSokResponse med http status: ${hentRestTjenesteTilTesting().hentHttpStatus()}")
        }
    }

    fun patchOppgave(medOppgaveId: MedOppgaveId) {
        hentRestTjenesteTilTesting().exchangePatch("/api/v1/oppgaver/${medOppgaveId.id}", medOppgaveId)
    }
}
