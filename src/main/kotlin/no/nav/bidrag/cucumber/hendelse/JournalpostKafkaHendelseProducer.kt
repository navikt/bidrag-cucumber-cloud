package no.nav.bidrag.cucumber.hendelse

import com.fasterxml.jackson.core.JsonProcessingException
import org.springframework.kafka.core.KafkaTemplate

class JournalpostKafkaHendelseProducer(private val kafkaTemplate: KafkaTemplate<String, String>, private val topic: String) {

    fun publish(journalpostId: String, hendelse: String) {
        try {
            kafkaTemplate.send(topic, journalpostId, hendelse)
        } catch (e: JsonProcessingException) {
            throw IllegalStateException(e.message, e)
        }
    }

}
