package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import no.nav.bidrag.cucumber.BidragCucumberCloud
import no.nav.bidrag.cucumber.RestTjeneste
import no.nav.bidrag.cucumber.RestTjenesteForApplikasjon
import no.nav.bidrag.cucumber.ScenarioManager
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService
import no.nav.bidrag.cucumber.cloud.arbeidsflyt.PrefiksetJournalpostIdForHendelse.Hendelse
import no.nav.bidrag.cucumber.hendelse.HendelseProducer
import no.nav.bidrag.cucumber.hendelse.JournalpostHendelse
import no.nav.bidrag.cucumber.model.CucumberTestsDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
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
import org.springframework.web.client.RestTemplate

@DisplayName("ArbeidsflytEgenskaperEndreFagomradeService")
@SpringBootTest(classes = [BidragCucumberCloud::class])
internal class ArbeidsflytEgenskaperEndreFagomradeServiceTest {

    private val baseUrl = "https://base"
    private val hendelse = Hendelse.AVVIK_ENDRE_FAGOMRADE
    private val journalpostId: Long = 1010101010
    private val naisApplikasjon: String = "oppgave"
    private val tema = "BID"

    @MockBean
    private lateinit var hendelseProducerMock: HendelseProducer

    @MockBean
    private lateinit var restTemplateMock: RestTemplate

    @BeforeEach
    fun `opprett prefikset journalpostId for hendelse`() {
        ArbeidsflytEgenskaper.prefiksetJournalpostIdForHendelse.opprett(hendelse, journalpostId, tema)
    }

    @BeforeEach
    fun konfigurerNaisApplikasjonForOppgave() {
        CucumberTestsDto(ingressesForApps = listOf("$baseUrl@$naisApplikasjon")).initCucumberEnvironment()

        val restTjenesteMedBaseUrl = RestTjeneste.ResttjenesteMedBaseUrl(restTemplateMock, baseUrl)

        RestTjenesteForApplikasjon.RestTjenesteForApplikasjonThreadLocal().hentEllerKonfigurer(naisApplikasjon) { restTjenesteMedBaseUrl }
        FellesEgenskaperService.settOppNaisApp(naisApplikasjon)
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
    fun `skal opprette oppgave`() {
        whenever(restTemplateMock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String::class.java)))
            .thenReturn(ResponseEntity.ok().build())
        whenever(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String::class.java)))
            .thenReturn(ResponseEntity.ok().body("""{"antallTreffTotalt":"0"}"""))

        val prefiksetJournalpostId = PrefiksetJournalpostIdForHendelse().hent(hendelse, tema)
        ArbeidsflytEgenskaperEndreFagomradeService.opprettOppgave(prefiksetJournalpostId, tema)

        verify(restTemplateMock).exchange(eq("/api/v1/oppgaver"), eq(HttpMethod.POST), any(), eq(String::class.java))
    }

    @Test
    fun `skal ikke opprette oppgave når den eksisterer fra før`() {
        whenever(restTemplateMock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String::class.java)))
            .thenReturn(ResponseEntity.ok().build())
        whenever(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String::class.java))).thenReturn(
            ResponseEntity.ok().body("""{"antallTreffTotalt":"1","oppgaver":[{"id":"1"}]}""")
        )

        val prefiksetJournalpostId = PrefiksetJournalpostIdForHendelse().hent(hendelse, tema)
        ArbeidsflytEgenskaperEndreFagomradeService.opprettOppgave(prefiksetJournalpostId, tema)

        verify(restTemplateMock, never()).exchange(eq("/api/v1/oppgaver"), eq(HttpMethod.POST), any(), eq(String::class.java))
    }

    @Test
    fun `skal sette sette status til UNDER_BEHANDLING på oppgave som eksisterer fra før`() {
        whenever(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String::class.java))).thenReturn(
            ResponseEntity.ok().body("""{"antallTreffTotalt":"1","oppgaver":[{"id":"1001","versjon":"1"}]}""")
        )

        val prefiksetJournalpostId = PrefiksetJournalpostIdForHendelse().hent(hendelse, tema)
        ArbeidsflytEgenskaperEndreFagomradeService.opprettOppgave(prefiksetJournalpostId, tema)

        @Suppress("UNCHECKED_CAST") val httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity::class.java) as ArgumentCaptor<HttpEntity<String>>

        verify(restTemplateMock).exchange(eq("/api/v1/oppgaver/1001"), eq(HttpMethod.PATCH), httpEntityCaptor.capture(), eq(String::class.java))

        assertThat(httpEntityCaptor.value.body).isEqualTo(""""{"versjon":"1","tema":"BID","status":"UNDER_BEHANDLING"}""")
    }
}
