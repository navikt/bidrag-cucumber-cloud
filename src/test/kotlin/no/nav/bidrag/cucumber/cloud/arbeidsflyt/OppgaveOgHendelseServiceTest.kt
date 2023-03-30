package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.cucumber.BidragCucumberCloud
import no.nav.bidrag.cucumber.FAGOMRADE_BIDRAG
import no.nav.bidrag.cucumber.hendelse.HendelseProducer
import no.nav.bidrag.cucumber.model.BidragCucumberSingletons
import no.nav.bidrag.cucumber.model.CucumberTestRun
import no.nav.bidrag.cucumber.model.CucumberTestsModel
import no.nav.bidrag.cucumber.model.JournalpostHendelse
import no.nav.bidrag.cucumber.model.PatchStatusOppgaveRequest
import no.nav.bidrag.cucumber.service.AzureTokenService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.anyString
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles

@DisplayName("OppgaveOgHendelseService")
@SpringBootTest(classes = [BidragCucumberCloud::class])
@ActiveProfiles("test")
internal class OppgaveOgHendelseServiceTest {

    private val baseUrl = "https://base"
    private val journalpostHendelse = JournalpostHendelse(
        journalpostId = "BID-1010101010",
        fagomrade = FAGOMRADE_BIDRAG
    )

    @MockBean
    private lateinit var azureTokenService: AzureTokenService

    @MockBean
    private lateinit var hendelseProducerMock: HendelseProducer

    @MockBean
    private lateinit var httpHeaderRestTemplateMock: HttpHeaderRestTemplate

    @BeforeEach
    fun konfigurerNaisApplikasjonForOppgave() {
        whenever(azureTokenService.generateToken(anyString(), any())).thenReturn("")
        whenever(azureTokenService.generateToken(anyString(), Mockito.isNull())).thenReturn("")
        val naisApplikasjon = "oppgave"
        CucumberTestRun(CucumberTestsModel(ingressesForApps = listOf("$baseUrl@$naisApplikasjon"))).initEnvironment()
        CucumberTestRun.settOppNaisAppTilTesting(naisApplikasjon)
    }

    @Test
    fun `skal opprette journalpostHendelse`() {
        CorrelationId.generateTimestamped("junit-test")
        OppgaveOgHendelseService.opprettJournalpostHendelse(journalpostHendelse)

        verify(hendelseProducerMock).publish(
            JournalpostHendelse(journalpostId = "BID-${journalpostHendelse.hentJournalpostIdUtenPrefix()}", fagomrade = "BID")
        )
    }

    @Test
    fun `skal opprette oppgave`() {
        whenever(httpHeaderRestTemplateMock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String::class.java)))
            .thenReturn(ResponseEntity.ok().build())
        whenever(httpHeaderRestTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String::class.java)))
            .thenReturn(ResponseEntity.ok().body("""{"antallTreffTotalt":"0"}"""))

        OppgaveOgHendelseService.tilbyOppgave(journalpostHendelse)

        verify(httpHeaderRestTemplateMock).exchange(eq("/api/v1/oppgaver"), eq(HttpMethod.POST), any(), eq(String::class.java))
    }

    @Test
    fun `skal ikke opprette oppgave når den eksisterer fra før`() {
        whenever(httpHeaderRestTemplateMock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String::class.java)))
            .thenReturn(ResponseEntity.ok().build())
        whenever(httpHeaderRestTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String::class.java))).thenReturn(
            ResponseEntity.ok().body("""{"antallTreffTotalt":"1","oppgaver":[{"id":"1","versjon":"1"}]}""")
        )

        OppgaveOgHendelseService.tilbyOppgave(journalpostHendelse)

        verify(httpHeaderRestTemplateMock, never()).exchange(eq("/api/v1/oppgaver"), eq(HttpMethod.POST), any(), eq(String::class.java))
    }

    @Test
    fun `skal sette sette status til UNDER_BEHANDLING på oppgave som eksisterer fra før`() {
        whenever(httpHeaderRestTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String::class.java))).thenReturn(
            ResponseEntity.ok().body("""{"antallTreffTotalt":"1","oppgaver":[{"id":"1001","versjon":"1"}]}""")
        )

        OppgaveOgHendelseService.tilbyOppgave(journalpostHendelse)

        @Suppress("UNCHECKED_CAST")
        val httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity::class.java)
            as ArgumentCaptor<HttpEntity<PatchStatusOppgaveRequest>>

        verify(httpHeaderRestTemplateMock).exchange(eq("/api/v1/oppgaver/1001"), eq(HttpMethod.PATCH), httpEntityCaptor.capture(), eq(String::class.java))

        assertThat(httpEntityCaptor.value.body).isEqualTo(
            BidragCucumberSingletons.toJson(
                PatchStatusOppgaveRequest(id = 1001, status = "UNDER_BEHANDLING", tema = FAGOMRADE_BIDRAG, versjon = 1, tildeltEnhetsnr = "4812")
            )
        )
    }

    @Test
    fun `skal gjenta rest-kall når det er gitt et maks antall ganger og ikke ønsket resultat fungerer`() {
        whenever(httpHeaderRestTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String::class.java))).thenReturn(
            ResponseEntity.ok().body("""{"antallTreffTotalt":"0","oppgaver":[]}""")
        ).thenReturn(
            ResponseEntity.ok().body("""{"antallTreffTotalt":"0","oppgaver":[]}""")
        ).thenReturn(
            ResponseEntity.ok().body("""{"antallTreffTotalt":"1","oppgaver":[{"id":"1001","versjon":"1","tildeltEnhetsnr":"123"}]}""")
        )

        OppgaveOgHendelseService.sokOpprettetOppgaveForHendelse(123, "BID", antallGjentakelser = 3)
        OppgaveOgHendelseService.assertThatOppgaveHar("123")
    }
}
