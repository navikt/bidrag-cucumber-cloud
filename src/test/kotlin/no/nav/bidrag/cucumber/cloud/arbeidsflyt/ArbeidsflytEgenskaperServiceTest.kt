package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import no.nav.bidrag.cucumber.BidragCucumberCloud
import no.nav.bidrag.cucumber.ScenarioManager
import no.nav.bidrag.cucumber.hendelse.HendelseProducer
import no.nav.bidrag.cucumber.hendelse.JournalpostHendelse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@DisplayName("ArbeidsflytEgenskaperService")
@SpringBootTest(classes = [BidragCucumberCloud::class])
internal class ArbeidsflytEgenskaperServiceTest {

    @MockBean
    private lateinit var hendelseProducerMock: HendelseProducer

    @BeforeEach
    fun `init CorrelationId`() {
        ScenarioManager.initCorrelationId()
    }

    @Test
    fun `skal opprette journalpostHendelse`() {
        val journalpostId = "BID-123456789"
        ArbeidsflytEgenskaperService.JOURNALPOST_IDs.set(journalpostId)
        ArbeidsflytEgenskaperService.opprettJournalpostHendelse("AVVIK_ENDRE_FAGOMRADE", mapOf("fagomrade" to "FAR"))

        verify(hendelseProducerMock).publish(
            JournalpostHendelse(journalpostId = journalpostId, hendelse = "AVVIK_ENDRE_FAGOMRADE", detaljer = mapOf("fagomrade" to "FAR"))
        )
    }
}