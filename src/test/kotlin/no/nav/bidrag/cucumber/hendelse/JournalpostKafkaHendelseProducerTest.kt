package no.nav.bidrag.cucumber.hendelse

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.cucumber.model.CucumberTestsDto
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate

@ExtendWith(MockitoExtension::class)
@DisplayName("JournalpostKafkaHendelseProducer")
internal class JournalpostKafkaHendelseProducerTest {
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
        CucumberTestsDto(sanityCheck = false).initCucumberEnvironment()
    }

    @Test
    @Disabled("failure on linux only... wip")
    fun `skal få timeout når kafka henger ved publisering`() {
        val journalpostHendelse = JournalpostHendelse(journalpostId = "BID-101", hendelse = "TEST")

        whenever(objectMapperMock.writeValueAsString(journalpostHendelse)).thenReturn("{}")
        whenever(kafkaTemplateMock.send(eq("test.topic"), anyString(), anyString())).then { Thread.sleep(1000) }

        val journalpostKafkaHendelseProducer = JournalpostKafkaHendelseProducer(
            kafkaTemplate = kafkaTemplateMock,
            objectMapper = objectMapperMock,
            timeoutAfterMs = 10,
            topic = "test.topic"
        )

        assertThatExceptionOfType(HendelseTimeoutException::class.java)
            .isThrownBy { journalpostKafkaHendelseProducer.publish(journalpostHendelse) }
            .withMessageContaining("Hendelse med timeout!")
    }
}