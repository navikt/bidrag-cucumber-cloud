package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService.hentRestTjeneste
import no.nav.bidrag.cucumber.model.BidragCucumberSingletons
import org.slf4j.LoggerFactory
import java.time.LocalDate

object OppgaveConsumer {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(OppgaveConsumer::class.java)

    fun opprettOppgave(journalpostId: String, tema: String) {
        sokOppgave(journalpostId, tema)
        hentRestTjeneste().exchangePost(
            "/api/v1/oppgaver",
            """
            {
              "journalpostId": "$journalpostId",
              "tema": "$tema",
              "oppgavetype": "JFR",
              "prioritet": "NORM",
              "aktivDato": "${LocalDate.now().minusDays(1)}"
            }
            """.trimIndent()
        )
    }

    fun sokOppgave(journalpostId: String, tema: String): OppgaveSokResponse? {
        hentRestTjeneste().exchangeGet("/api/v1/oppgaver?journalpostId=$journalpostId&statuskategori=AAPEN&tema=$tema")

        try {
            return BidragCucumberSingletons.objectMapper?.readValue(hentRestTjeneste().hentResponse(), OppgaveSokResponse::class.java)
        } finally {
            LOGGER.info("${
                if (hentRestTjeneste().responseEntity != null) "Har " else "Mangler " 
            }OppgaveSokResponse (${hentRestTjeneste().hentResponse()} med http status: ${hentRestTjeneste().hentHttpStatus()})")
        }
    }

    fun settOppgaveTilUnderBehandling(id: Int, tema: String, versjon: String) {
        hentRestTjeneste().exchangePatch("/api/v1/oppgaver/$id", """"{"versjon":"$versjon","tema":"$tema","status":"UNDER_BEHANDLING"}""")
    }

    data class OppgaveSokResponse(var antallTreffTotalt: Int = -1, var oppgaver: List<Oppgave> = emptyList())
    data class Oppgave(var id: Int = -1, var versjon: String = "na")
}
