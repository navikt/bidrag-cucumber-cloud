package no.nav.bidrag.cucumber.hendelse

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
    private val timeoutAfterSeconds: Long = 15
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
        } catch (e: Exception) {
            ScenarioManager.errorLog("Publisering av $journalpostHendelse feilet!", e)
            throw e
        }
    }

    private fun sendKafkaMelding(publish: Publish) {
        kafkaTemplate.send(topic, publish.journalpostId, publish.json)
    }

    private fun publishWithTimeout(publish: Publish, doSend: (input: Publish) -> Unit) {
        val start = LocalDateTime.now()
        val timeout = LocalDateTime.now().plusSeconds(timeoutAfterSeconds)
        val future = CompletableFuture.runAsync {
            doSend(publish)
        }

        while (!future.isDone) {
            if (LocalDateTime.now().isBefore(timeout)) {
                Thread.sleep(500)
            } else {
                val hendelseTimeoutException = HendelseTimeoutException(start, LocalDateTime.now())
                ScenarioManager.errorLog(hendelseTimeoutException.message!!, hendelseTimeoutException)

                throw hendelseTimeoutException
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
    val sporing: Sporingsdata = Sporingsdata(correlationId = CorrelationId.fetchCorrelationIdForThread(), brukerident = Environment.testUsername),
    val detaljer: Map<String, String?> = emptyMap()
) {
    constructor(detaljer: Map<String, String>, hendelse: Hendelse, tema: String) : this(
        detaljer = detaljer,
        journalpostId = JournalpostIdForOppgave.hentPrefiksetJournalpostId(hendelse, tema),
        hendelse = hendelse.name
    )
}

data class Sporingsdata(val correlationId: String, val brukerident: String? = null) {
    @Suppress("unused") // brukes av jackson (and not part of equals/hashcode for data class)
    val opprettet: LocalDateTime = LocalDateTime.now()
}
