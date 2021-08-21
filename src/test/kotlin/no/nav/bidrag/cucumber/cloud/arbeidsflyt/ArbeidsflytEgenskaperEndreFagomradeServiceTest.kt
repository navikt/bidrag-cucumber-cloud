package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import no.nav.bidrag.cucumber.BidragCucumberCloud
import no.nav.bidrag.cucumber.ScenarioManager
import no.nav.bidrag.cucumber.hendelse.HendelseProducer
import no.nav.bidrag.cucumber.hendelse.JournalpostHendelse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.mockito.kotlin.verify
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@DisplayName("ArbeidsflytEgenskaperEndreFagomradeService")
@SpringBootTest(classes = [BidragCucumberCloud::class])
internal class ArbeidsflytEgenskaperEndreFagomradeServiceTest {

    private val hendelse = Hendelse.AVVIK_ENDRE_FAGOMRADE
    private val journalpostId: Long = 1010101010
    private val tema = "BID"

    @MockBean
    private lateinit var hendelseProducerMock: HendelseProducer

    @BeforeEach
    fun `opprett prefikset journalpostId for hendelse`() {
        ArbeidsflytEgenskaper.prefiksetJournalpostIdForHendelse.opprett(hendelse, journalpostId, tema)
    }

    @BeforeEach
    fun `init CorrelationId`() {
        ScenarioManager.initCorrelationId()
    }

    @Test
    fun `skal opprette journalpostHendelse`() {
        ArbeidsflytEgenskaperEndreFagomradeService.opprettJournalpostHendelse(hendelse, mapOf("fagomrade" to "FAR"), tema)

        verify(hendelseProducerMock).publish(
            JournalpostHendelse(journalpostId = "$tema-$journalpostId", hendelse = hendelse.name, detaljer = mapOf("fagomrade" to "FAR"))
        )
    }

    @Test
    fun `skal ikke opprette oppgave for hendelse når den eksisterer fra før`() {
        fail("wip")
    }

    @Test
    fun `skal opprette oppgave for hendelse`() {
        fail("wip")
    }

    @Test
    fun `når oppgave finnes for hendelse, skal oppgaven patches slik at den stemmer er klar for testing på hendelsen`() {
        fail("wip")
    }
}