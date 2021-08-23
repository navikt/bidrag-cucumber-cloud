package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService.hentRestTjeneste
import no.nav.bidrag.cucumber.cloud.arbeidsflyt.PrefiksetJournalpostIdForHendelse.Hendelse
import no.nav.bidrag.cucumber.hendelse.JournalpostHendelse
import no.nav.bidrag.cucumber.model.BidragCucumberSingletons
import org.slf4j.LoggerFactory
import java.time.LocalDate

/**
 * Service class in order to loosely couple logic from cucumber infrastructure
 */
object ArbeidsflytEgenskaperEndreFagomradeService {
    private val LOGGER = LoggerFactory.getLogger(ArbeidsflytEgenskaperEndreFagomradeService::class.java)

    fun opprettOppgave(hendelse: Hendelse, journalpostId: Long, tema: String) {
        ArbeidsflytEgenskaper.prefiksetJournalpostIdForHendelse.opprett(hendelse, journalpostId, tema)
        opprettOppgave(hendelse, tema)
    }

    private fun opprettOppgave(hendelse: Hendelse, tema: String) {
        hentRestTjeneste().exchangePost(
            "/api/v1/oppgaver",
            """
            {
              "journalpostId": "${ArbeidsflytEgenskaper.hentId(hendelse, tema)}",
              "tema": "$tema",
              "oppgavetype": "JFR",
              "prioritet": "NORM",
              "aktivDato": "${LocalDate.now().minusDays(1)}"
            }
            """.trimIndent()
        )
    }

    fun opprettJournalpostHendelse(hendelse: Hendelse, detaljer: Map<String, String>, tema: String) {
        val journalpostHendelse = JournalpostHendelse(detaljer, hendelse, tema)

        BidragCucumberSingletons.hendelseProducer?.publish(journalpostHendelse)
            ?: LOGGER.warn("Cannot publish $hendelse when spring context is not initialized")

        Thread.sleep(500) // for å sørge for at kafka melding blir behandlet i bidrag-arbeidsflyt
    }

    fun sokOppgaveForHendelse(hendelse: Hendelse, tema: String) = hentRestTjeneste().exchangeGet(
        "/api/v1/oppgaver?journalpostId=${ArbeidsflytEgenskaper.hentId(hendelse, tema)}&statuskategori=AAPEN"
    )
}
