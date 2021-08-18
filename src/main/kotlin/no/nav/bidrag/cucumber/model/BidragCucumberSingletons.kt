package no.nav.bidrag.cucumber.model

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.hendelse.HendelseProducer
import no.nav.bidrag.cucumber.hendelse.JournalpostHendelse
import org.slf4j.LoggerFactory

/**
 * Data/singletons som er gyldige i en cucumber-kjøring og som er felles for ALLE egenskaper definert i en feature-filer
 */
internal object BidragCucumberSingletons {
    private val LOGGER = LoggerFactory.getLogger(BidragCucumberSingletons::class.java)

    lateinit var hendelseProducer: HendelseProducer
    lateinit var objectMapper: ObjectMapper

    fun publishWhenNotSanityCheck(journalpostHendelse: JournalpostHendelse) {
        if (Environment.isNotSanityCheck()) {
            hendelseProducer.publish(journalpostHendelse)
        } else {
            LOGGER.info("SanityCheck - Hendelse publiseres ikke: $journalpostHendelse")
        }
    }
}
