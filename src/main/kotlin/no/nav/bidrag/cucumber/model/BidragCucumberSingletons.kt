package no.nav.bidrag.cucumber.model

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.cucumber.hendelse.HendelseProducer

/**
 * Data/singletons som er gyldige i en cucumber-kjøring og som er felles for ALLE egenskaper definert i en feature-filer
 */
internal object BidragCucumberSingletons {
    var hendelseProducer: HendelseProducer? = null
}
