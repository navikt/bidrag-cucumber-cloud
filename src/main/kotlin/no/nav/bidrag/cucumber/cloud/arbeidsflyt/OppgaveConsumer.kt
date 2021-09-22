package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService.hentRestTjeneste
import no.nav.bidrag.cucumber.model.BidragCucumberSingletons
import org.slf4j.LoggerFactory
import java.time.LocalDate

object OppgaveConsumer {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(OppgaveConsumer::class.java)

    fun opprettOppgave(journalpostId: String, tema: String) {
        hentRestTjeneste().exchangePost(
            "/api/v1/oppgaver",
            """
            {
              "journalpostId": "$journalpostId",
              "tema": "$tema",
              "oppgavetype": "JFR",
              "prioritet": "NORM",
              "aktivDato": "${LocalDate.now().minusDays(1)}",
              "tildeltEnhetsnr": "1001"
            }
            """.trimIndent()
        )
    }

    fun sokOppgave(journalpostId: String, tema: String): OppgaveSokResponse? {
        hentRestTjeneste().exchangeGet("/api/v1/oppgaver?journalpostId=$journalpostId&statuskategori=AAPEN&tema=$tema")

        try {
            val response = hentRestTjeneste().hentResponse() ?: return null
            return BidragCucumberSingletons.objectMapper?.readValue(response, OppgaveSokResponse::class.java)
        } finally {
            val oppgaveSokResponse = if (hentRestTjeneste().responseEntity != null) {
                "Har OppgaveSokResponse (${hentRestTjeneste().hentResponse()})"
            } else {
                "Mangler OppgaveSokResponse ${if (Environment.isSanityCheck) "siden det er kjøring av sanity check" else "og det er ikke sanity check"}!"
            }

            LOGGER.info("$oppgaveSokResponse med http status: ${hentRestTjeneste().hentHttpStatus()}")
        }
    }

    fun settOppgaveTilUnderBehandling(id: Int, tema: String, versjon: String) {
        hentRestTjeneste().exchangePatch("/api/v1/oppgaver/$id", """"{"versjon":"$versjon","tema":"$tema","status":"UNDER_BEHANDLING"}""")
    }

    data class OppgaveSokResponse(var antallTreffTotalt: Int = 0, var oppgaver: List<Oppgave> = emptyList())
    data class Oppgave(var id: Int = -1, var versjon: String = "na")
}
