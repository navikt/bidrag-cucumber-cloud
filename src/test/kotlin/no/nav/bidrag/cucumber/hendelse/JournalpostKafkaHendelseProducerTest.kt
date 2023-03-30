package no.nav.bidrag.cucumber.hendelse

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.cucumber.model.CucumberTestsModel
import no.nav.bidrag.cucumber.model.HendelseTimeoutException
import no.nav.bidrag.cucumber.model.JournalpostHendelse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@DisplayName("JournalpostKafkaHendelseProducer")
internal class JournalpostKafkaHendelseProducerTest {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(JournalpostKafkaHendelseProducerTest::class.java)
    }

    @Mock
    private lateinit var kafkaTemplateMock: KafkaTemplate<String, String>

    @Mock
    private lateinit var objectMapperMock: ObjectMapper

    @BeforeEach
    fun `set correlation id for thread`() {
        CorrelationId.generateTimestamped("test")
    }

    @BeforeEach
    fun `skal ikke være sanity check`() {
        CucumberTestsModel(sanityCheck = false).initCucumberEnvironment()
    }

    @Test
    fun `skal få timeout når kafka henger ved publisering`() {
        val journalpostHendelse = JournalpostHendelse(journalpostId = "BID-101")

        whenever(objectMapperMock.writeValueAsString(journalpostHendelse)).thenReturn("{}")
        lenient().`when`(kafkaTemplateMock.send(eq("test.topic"), anyString(), anyString())).then { Thread.sleep(5000) }

        val journalpostKafkaHendelseProducer = JournalpostKafkaHendelseProducer(
            kafkaTemplate = kafkaTemplateMock,
            objectMapper = objectMapperMock,
            timeoutAfterSeconds = 1,
            topic = "test.topic"
        )

        assertThatExceptionOfType(HendelseTimeoutException::class.java)
            .isThrownBy { journalpostKafkaHendelseProducer.publish(journalpostHendelse) }
            .withMessageContaining("Hendelse med timeout!")
    }

    @Test
    fun `skal ikke få timeout når kafka publiserer hendelse`() {
        val journalpostHendelse = JournalpostHendelse(journalpostId = "BID-101")
        val start = LocalDateTime.now()

        whenever(objectMapperMock.writeValueAsString(journalpostHendelse)).thenReturn("{}")
        lenient().`when`(kafkaTemplateMock.send(eq("test.topic"), anyString(), anyString())).then { LOGGER.info("publiserer hendelse...") }

        val journalpostKafkaHendelseProducer = JournalpostKafkaHendelseProducer(
            kafkaTemplate = kafkaTemplateMock,
            objectMapper = objectMapperMock,
            timeoutAfterSeconds = 10,
            topic = "test.topic"
        )

        journalpostKafkaHendelseProducer.publish(journalpostHendelse)

        val slutt = LocalDateTime.now()

        assertThat(slutt.minusSeconds(10)).`as`("${slutt.minusSeconds(10)} er tidligere enn $start ").isBefore(start)
    }
}
