package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService.hentRestTjeneste
import no.nav.bidrag.cucumber.hendelse.JournalpostHendelse
import no.nav.bidrag.cucumber.model.BidragCucumberSingletons
import org.slf4j.LoggerFactory
import java.time.LocalDate

/**
 * Service class in order to loosely couple logic from cucumber infrastructure
 */
object ArbeidsflytEgenskaperService {
    internal val JOURNALPOST_IDs = ThreadLocal<String>()
    private val LOGGER = LoggerFactory.getLogger(ArbeidsflytEgenskaperService::class.java)

    fun opprettOppgave(journalpostId: Long, tema: String) {
        JOURNALPOST_IDs.set("$tema-$journalpostId")

        hentRestTjeneste().exchangePost(
            "/api/v1/oppgaver",
            """
              {
                "journalpostId": "${JOURNALPOST_IDs.get()}",
                "tema": "$tema",
                "oppgavetype": "JFR",
                "prioritet": "NORM",
                "aktivDato": "${LocalDate.now().minusDays(1)}"
              }
              """.trimIndent()
        )
    }

    fun opprettJournalpostHendelse(hendelse: String, detaljer: Map<String, String>) {
        BidragCucumberSingletons.hendelseProducer?.publish(
            JournalpostHendelse(
                journalpostId = JOURNALPOST_IDs.get(),
                hendelse = hendelse,
                detaljer = detaljer
            )
        ) ?: LOGGER.warn("Cannot publish $hendelse when spring context is not initialized")

        Thread.sleep(500) // for å sørge for at kafka melding blir behandlet i bidrag-arbeidsflyt
    }

    fun sokOppgave() {
        hentRestTjeneste().exchangeGet("/api/v1/oppgaver?journalpostId=${JOURNALPOST_IDs.get()}&statuskategori=AAPEN")
    }
}
