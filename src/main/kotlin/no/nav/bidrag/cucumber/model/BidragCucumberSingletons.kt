package no.nav.bidrag.cucumber.model

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.cucumber.SpringConfig
import no.nav.bidrag.cucumber.hendelse.HendelseProducer
import org.springframework.context.ApplicationContext

/**
 * Singletons som er gyldige i en cucumber-kjøring og som er felles for ALLE egenskaper definert i en feature-filer
 */
internal object BidragCucumberSingletons {
    var hendelseProducer: HendelseProducer? = null
    var objectMapper: ObjectMapper? = null
    private var applicationContext: ApplicationContext? = null

    fun addContextFromSpring(applicationContext: ApplicationContext) {
        BidragCucumberSingletons.applicationContext = applicationContext
    }

    fun hentPrototypeFraApplicationContext() = applicationContext?.getBean(HttpHeaderRestTemplate::class.java) ?: doManualInit()

    private fun doManualInit(): HttpHeaderRestTemplate {
        val httpComponentsClientHttpRequestFactory = SpringConfig().httpComponentsClientHttpRequestFactorySomIgnorererHttps()
        return HttpHeaderRestTemplate(httpComponentsClientHttpRequestFactory)
    }
}
