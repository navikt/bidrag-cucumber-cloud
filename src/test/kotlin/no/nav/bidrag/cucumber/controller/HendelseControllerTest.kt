package no.nav.bidrag.cucumber.controller

import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.cucumber.BidragCucumberCloudLocal
import no.nav.bidrag.cucumber.hendelse.HendelseProducer
import no.nav.bidrag.cucumber.model.JournalpostHendelse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [BidragCucumberCloudLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("HendelseController (mocked HendelseProducer)")
@ActiveProfiles("test")
internal class HendelseControllerTest {
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @MockBean
    private lateinit var hendelseProducerMock: HendelseProducer

    @BeforeEach
    fun `sett CorrelationId for thread`() {
        CorrelationId.generateTimestamped("HendelseController")
    }

    @Test
    fun `skal publisere melding med HendelseProducer`() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val testResponse =
            testRestTemplate.postForEntity(
                "/hendelse/opprett",
                HttpEntity(
                    """
                {
                  "journalpostId":"1001",
                  "brukerident":"jumbo"
                }
                    """.trimMargin().trim(),
                    headers,
                ),
                Void::class.java,
            )

        assertThat(testResponse.statusCode).isEqualTo(HttpStatus.OK)

        verify(hendelseProducerMock).publish(JournalpostHendelse(journalpostId = "1001"))
    }
}
