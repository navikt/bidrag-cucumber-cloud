package no.nav.bidrag.cucumber.hendelse

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.cucumber.ScenarioManager
import no.nav.bidrag.cucumber.model.CucumberTestRun
import no.nav.bidrag.cucumber.model.HendelseTimeoutException
import no.nav.bidrag.cucumber.model.JournalpostHendelse
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

class JournalpostKafkaHendelseProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val topic: String,
    private val objectMapper: ObjectMapper,
    private val timeoutAfterSeconds: Long = 15,
) : HendelseProducer {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(JournalpostKafkaHendelseProducer::class.java)
    }

    override fun publish(journalpostHendelse: JournalpostHendelse) {
        try {
            if (CucumberTestRun.isNotSanityCheck) {
                LOGGER.info("Publish $journalpostHendelse til topic $topic")
                publishWithTimeout(
                    publish = Publish(journalpostHendelse.journalpostId, objectMapper.writeValueAsString(journalpostHendelse)),
                    doSend = this::sendKafkaMelding,
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

    private fun publishWithTimeout(
        publish: Publish,
        doSend: (input: Publish) -> Unit,
    ) {
        val start = LocalDateTime.now()
        val timeout = LocalDateTime.now().plusSeconds(timeoutAfterSeconds)
        val future =
            CompletableFuture.runAsync {
                doSend(publish)
            }

        while (!future.isDone) {
            if (LocalDateTime.now().isBefore(timeout)) {
                Thread.sleep(500)
            } else {
                val hendelseTimeoutException = HendelseTimeoutException(start, LocalDateTime.now())
                LOGGER.error(hendelseTimeoutException.message)

                throw hendelseTimeoutException
            }
        }
    }

    private data class Publish(val journalpostId: String, val json: String)
}

interface HendelseProducer {
    fun publish(journalpostHendelse: JournalpostHendelse)
}
