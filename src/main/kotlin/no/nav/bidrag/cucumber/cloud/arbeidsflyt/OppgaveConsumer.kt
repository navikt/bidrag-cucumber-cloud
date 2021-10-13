package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService.hentRestTjeneste
import no.nav.bidrag.cucumber.model.BidragCucumberSingletons
import no.nav.bidrag.cucumber.model.MedOppgaveId
import no.nav.bidrag.cucumber.model.OppgaveSokResponse
import org.slf4j.LoggerFactory

object OppgaveConsumer {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(OppgaveConsumer::class.java)

    fun opprettOppgave(oppgave: Any) {
        LOGGER.info("oppretter oppgave: $oppgave")

        hentRestTjeneste().exchangePost("/api/v1/oppgaver", oppgave)
    }

    fun sokOppgave(journalpostId: Long, tema: String): OppgaveSokResponse {
        hentRestTjeneste()
            .exchangeGet("/api/v1/oppgaver?journalpostId=$journalpostId&journalpostId=$tema-$journalpostId&statuskategori=AAPEN&tema=$tema")

        try {
            val response = hentRestTjeneste().hentResponse() ?: return OppgaveSokResponse()

            return if (Environment.isSanityCheck) {
                OppgaveSokResponse()
            } else {
                BidragCucumberSingletons.readValue(response, OppgaveSokResponse::class.java)
            }
        } finally {
            val oppgaveSokResponse = if (hentRestTjeneste().responseEntity != null) {
                "Har OppgaveSokResponse (${hentRestTjeneste().hentResponse()})"
            } else {
                "Mangler OppgaveSokResponse ${if (Environment.isSanityCheck) "siden det er kjøring av sanity check" else "og det er ikke sanity check"}!"
            }

            LOGGER.info("$oppgaveSokResponse med http status: ${hentRestTjeneste().hentHttpStatus()}")
        }
    }

    fun patchOppgave(medOppgaveId: MedOppgaveId) {
        hentRestTjeneste().exchangePatch("/api/v1/oppgaver/${medOppgaveId.id}", medOppgaveId)
    }
}
