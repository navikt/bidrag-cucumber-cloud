package no.nav.bidrag.cucumber.hendelse

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.cucumber.Environment
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import java.time.LocalDateTime

class JournalpostKafkaHendelseProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val topic: String,
    private val objectMapper: ObjectMapper
) : HendelseProducer {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(JournalpostKafkaHendelseProducer::class.java)
    }

    override fun publish(journalpostHendelse: JournalpostHendelse) {
        try {
            if (Environment.isNotSanityCheck()) {
                kafkaTemplate.send(topic, journalpostHendelse.journalpostId, objectMapper.writeValueAsString(journalpostHendelse))
            } else {
                LOGGER.info("SanityCheck - Hendelse publiseres ikke: $journalpostHendelse")
            }
        } catch (e: JsonProcessingException) {
            throw IllegalStateException(e.message, e)
        }
    }
}

interface HendelseProducer {

    fun publish(journalpostHendelse: JournalpostHendelse)
}

data class JournalpostHendelse(
    val journalpostId: String,
    val hendelse: String,
    val sporing: Sporingsdata = Sporingsdata(CorrelationId.fetchCorrelationIdForThread()),
    val detaljer: Map<String, String?> = emptyMap()
) {
    init {
        sporing.brukerident = Environment.testUsername
    }
}

data class Sporingsdata(val correlationId: String) {
    var brukerident: String? = null

    @Suppress("unused") // brukes av jackson
    val opprettet: LocalDateTime = LocalDateTime.now()
}
