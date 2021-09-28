package no.nav.bidrag.cucumber.hendelse

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.ScenarioManager
import no.nav.bidrag.cucumber.cloud.arbeidsflyt.JournalpostIdForOppgave
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

class JournalpostKafkaHendelseProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val topic: String,
    private val objectMapper: ObjectMapper,
    private val timeoutAfterMs: Long = 15000
) : HendelseProducer {

    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(JournalpostKafkaHendelseProducer::class.java)
    }

    override fun publish(journalpostHendelse: JournalpostHendelse) {
        try {
            if (Environment.isNotSanityCheck()) {
                LOGGER.info("Publish $journalpostHendelse")
                publishWithTimeout(
                    publish = Publish(journalpostHendelse.journalpostId, objectMapper.writeValueAsString(journalpostHendelse)),
                    doSend = this::sendKafkaMelding
                )
            } else {
                LOGGER.info("SanityCheck - Hendelse publiseres ikke: $journalpostHendelse")
            }
        } catch (e: JsonProcessingException) {
            ScenarioManager.errorLog("Publisering av $journalpostHendelse feilet!", e)
            throw IllegalStateException(e.message, e)
        }
    }

    private fun sendKafkaMelding(publish: Publish) {
        kafkaTemplate.send(topic, publish.journalpostId, publish.json)
    }

    private fun publishWithTimeout(publish: Publish, doSend: (input: Publish) -> Unit) {
        val start = LocalDateTime.now()
        val timeout = LocalDateTime.now().plusNanos(timeoutAfterMs * 1000)
        val future = CompletableFuture.runAsync {
            doSend(publish)
        }

        while (!future.isDone) {
            if (LocalDateTime.now().isBefore(timeout)) {
                Thread.sleep(10)
            } else {
                throw HendelseTimeoutException(start, timeout)
            }
        }
    }

    private data class Publish(val journalpostId: String, val json: String)

}

interface HendelseProducer {
    fun publish(journalpostHendelse: JournalpostHendelse)
}

class HendelseTimeoutException(start: LocalDateTime, timeout: LocalDateTime) : RuntimeException(
    "Hendelse med timeout! Start: $start, timeout: $timeout"
)

data class JournalpostHendelse(
    val journalpostId: String,
    val hendelse: String,
    val sporing: Sporingsdata = Sporingsdata(correlationId = CorrelationId.fetchCorrelationIdForThread()),
    val detaljer: Map<String, String?> = emptyMap()
) {
    constructor(detaljer: Map<String, String>, hendelse: Hendelse, tema: String) : this(
        detaljer = detaljer,
        journalpostId = JournalpostIdForOppgave.hentPrefiksetJournalpostId(hendelse, tema),
        hendelse = hendelse.name
    )

    init {
        sporing.brukerident = Environment.testUsername
    }
}

data class Sporingsdata(val correlationId: String, var brukerident: String? = null) {
    @Suppress("unused") // brukes av jackson (and not part of equals/hashcode for data class)
    val opprettet: LocalDateTime = LocalDateTime.now()
}
