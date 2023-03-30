package no.nav.bidrag.cucumber.model

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.cucumber.BidragCucumberCloud
import no.nav.bidrag.cucumber.hendelse.HendelseProducer
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.http.ResponseEntity
import kotlin.reflect.KClass

/**
 * Singletons som er gyldige i en cucumber-kjøring og som er felles for ALLE egenskaper definert i feature-filer
 */
internal object BidragCucumberSingletons {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(BidragCucumberSingletons::class.java)

    private var applicationContext: ApplicationContext? = null
    private var exceptionLogger: ExceptionLogger? = null
    private var hendelseProducer: HendelseProducer? = null
    private var objectMapper: ObjectMapper? = null

    @Suppress("UNCHECKED_CAST")
    fun <T> hentEllerInit(kClass: KClass<*>): T = applicationContext?.getBean(kClass.java) as T? ?: init(kClass)
    fun hentPrototypeFraApplicationContext() = applicationContext?.getBean(HttpHeaderRestTemplate::class.java) ?: doManualInit()
    private fun fetchObjectMapper() = objectMapper ?: ObjectMapper()

    @Suppress("UNCHECKED_CAST")
    private fun <T> init(kClass: KClass<*>): T {
        if (kClass == ExceptionLogger::class) {
            return ExceptionLogger("${BidragCucumberCloud::class.simpleName}") as T
        }

        throw IllegalStateException("Mangler manuell initialisering av ${kClass.simpleName}")
    }

    private fun doManualInit(): HttpHeaderRestTemplate {
        return HttpHeaderRestTemplate()
    }

    fun publiserHendelse(journalpostHendelse: JournalpostHendelse) {
        hendelseProducer?.publish(journalpostHendelse) ?: LOGGER.warn(
            "Cannot publish $journalpostHendelse when spring context is not initialized, sanity check: ${CucumberTestRun.isSanityCheck}"
        )
    }

    fun mapResponseSomMap(responseEntity: ResponseEntity<String?>?): Map<String, Any> {
        return if (responseEntity?.statusCode?.is2xxSuccessful == true && responseEntity.body != null) {
            mapResponseSomMap(responseEntity.body!!)
        } else {
            HashMap()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapResponseSomMap(body: String): Map<String, Any> = try {
        fetchObjectMapper().readValue(body, Map::class.java) as Map<String, Any>
    } catch (e: Exception) {
        CucumberTestRun.holdExceptionForTest(e)
        throw e
    }

    fun <T> readValue(value: String, mapClass: Class<T>): T = objectMapper?.readValue(value, mapClass) ?: throw IllegalStateException(
        "Kunne ikke mappe: $value"
    )

    fun toJson(body: Any): String = objectMapper?.writeValueAsString(body) ?: """{ "noMappingAvailable":"$body" }"""

    fun setApplicationContext(applicationContext: ApplicationContext) {
        BidragCucumberSingletons.applicationContext = applicationContext
    }

    fun setExceptionLogger(exceptionLogger: ExceptionLogger) {
        BidragCucumberSingletons.exceptionLogger = exceptionLogger
    }

    fun setHendelseProducer(hendelseProducer: HendelseProducer) {
        BidragCucumberSingletons.hendelseProducer = hendelseProducer
    }

    fun setObjectMapper(objectMapper: ObjectMapper) {
        BidragCucumberSingletons.objectMapper = objectMapper
    }
}
