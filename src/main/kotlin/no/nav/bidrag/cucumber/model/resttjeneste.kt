package no.nav.bidrag.cucumber.model

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.commons.web.EnhetFilter
import no.nav.bidrag.cucumber.ScenarioManager
import no.nav.bidrag.cucumber.service.AzureTokenService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriTemplateHandler
import java.net.URI

internal class RestTjenesteForApplikasjon {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(CucumberTestRun::class.java)

        internal fun konfigurerApplikasjonUrlFor(applicationName: String): String {
            val ingress = CucumberTestRun.fetchIngress(applicationName)

            if (CucumberTestRun.isNoContextPathForApp(applicationName)) {
                return ingress
            }

            val ingressUrl = if (ingress.endsWith('/')) ingress.removeSuffix("/") else ingress

            return "$ingressUrl/$applicationName"
        }
    }

    private val resttjenesteForNavn: MutableMap<String, ResttjenesteMedBaseUrl> = HashMap()

    fun hentEllerKonfigurer(applicationName: String, konfigurer: () -> ResttjenesteMedBaseUrl): ResttjenesteMedBaseUrl {
        return resttjenesteForNavn.computeIfAbsent(applicationName) { konfigurer() }
    }

    fun konfigurerResttjeneste(applicationName: String): ResttjenesteMedBaseUrl {
        val applicationUrl = konfigurerApplikasjonUrlFor(applicationName)
        val httpHeaderRestTemplate = BidragCucumberSingletons.hentPrototypeFraApplicationContext()

        httpHeaderRestTemplate.uriTemplateHandler = BaseUrlTemplateHandler(applicationUrl)

        if (CucumberTestRun.isTestUserPresent) {
            val tokenService = BidragCucumberSingletons.hentFraContext(AzureTokenService::class) as AzureTokenService? ?: throw notNullTokenService()
            httpHeaderRestTemplate.addHeaderGenerator(HttpHeaders.AUTHORIZATION) { tokenService.generateBearerToken(applicationName) }
        } else {
            LOGGER.info("No user to provide security for when accessing $applicationName")
        }

        return ResttjenesteMedBaseUrl(httpHeaderRestTemplate, applicationUrl)
    }

    private fun notNullTokenService() = IllegalStateException("No token service in spring context")
}

internal class BaseUrlTemplateHandler(private val baseUrl: String) : UriTemplateHandler {
    override fun expand(uriTemplate: String, uriVariables: MutableMap<String, *>): URI {
        if (uriVariables.isNotEmpty()) {
            val queryString = StringBuilder()
            uriVariables.forEach { if (queryString.length == 1) queryString.append("$it") else queryString.append("?$it") }

            return URI.create(baseUrl + uriTemplate + queryString)
        }

        return URI.create(baseUrl + uriTemplate)
    }

    override fun expand(uriTemplate: String, vararg uriVariables: Any?): URI {
        if (uriVariables.isNotEmpty() && (uriVariables.size != 1 && uriVariables.first() != null)) {
            val queryString = StringBuilder("&")
            uriVariables.forEach {
                if (it != null && queryString.length == 1) {
                    queryString.append("$it")
                } else if (it != null) {
                    queryString.append("?$it")
                }
            }

            return URI.create(baseUrl + uriTemplate + queryString)
        }

        return URI.create(baseUrl + uriTemplate)
    }
}

class RestTjeneste(
    internal val rest: ResttjenesteMedBaseUrl
) {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(RestTjeneste::class.java)
    }

    private lateinit var fullUrl: FullUrl
    internal var responseEntity: ResponseEntity<String?>? = null

    constructor(naisApplication: String) : this(CucumberTestRun.hentEllerKonfigurerResttjeneste(naisApplication))

    fun hentFullUrlMedEventuellWarning() = "$fullUrl${appendWarningWhenExists()}"
    fun hentHttpStatus(): HttpStatus = responseEntity?.statusCode ?: HttpStatus.I_AM_A_TEAPOT
    fun hentResponse(): String? = responseEntity?.body
    fun hentResponseSomMap(): Map<String, Any> = if (responseEntity?.statusCode == HttpStatus.OK && responseEntity?.body != null)
        mapResponseBody(responseEntity?.body!!)
    else
        HashMap()

    @Suppress("UNCHECKED_CAST")
    private fun mapResponseBody(body: String): Map<String, Any> = try {
        ObjectMapper().readValue(body, Map::class.java) as Map<String, Any>
    } catch (e: Exception) {
        CucumberTestRun.holdExceptionForTest(e)
        throw e
    }

    private fun appendWarningWhenExists(): String {
        val warnings = responseEntity?.headers?.get(HttpHeaders.WARNING) ?: emptyList()

        return if (warnings.isNotEmpty()) " - ${warnings[0]}" else ""
    }

    fun exchangeGet(endpointUrl: String): ResponseEntity<String?> {
        fullUrl = FullUrl(rest.baseUrl, endpointUrl)

        val header = initHttpHeadersWithCorrelationIdAndEnhet()

        exchange(HttpEntity(null, header), endpointUrl, HttpMethod.GET)

        LOGGER.info(
            if (responseEntity?.body != null) "response with body and status ${responseEntity!!.statusCode}"
            else if (responseEntity == null) "no response entity (${sanityCheck()})" else "no response body with status ${responseEntity!!.statusCode}"
        )

        return responseEntity ?: ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build()
    }

    private fun sanityCheck(): String {
        return if (CucumberTestRun.isSanityCheck) "is sanity check" else "is NOT sanity check"
    }

    private fun initHttpHeadersWithCorrelationIdAndEnhet(): HttpHeaders {
        val headers = HttpHeaders()
        headers.add(CorrelationId.CORRELATION_ID_HEADER, ScenarioManager.getCorrelationIdForScenario())
        headers.add(EnhetFilter.X_ENHET_HEADER, "4802")

        return headers
    }

    fun exchangePost(endpointUrl: String, body: Any) {
        val jsonEntity = httpEntity(endpointUrl, BidragCucumberSingletons.toJson(body))
        exchange(jsonEntity, endpointUrl, HttpMethod.POST)
    }

    fun exchangePatch(endpointUrl: String, body: Any) {
        val jsonEntity = httpEntity(endpointUrl, BidragCucumberSingletons.toJson(body))
        exchange(jsonEntity, endpointUrl, HttpMethod.PATCH)
    }

    private fun httpEntity(endpointUrl: String, body: Any): HttpEntity<*> {
        this.fullUrl = FullUrl(rest.baseUrl, endpointUrl)
        val headers = initHttpHeadersWithCorrelationIdAndEnhet()
        headers.contentType = MediaType.APPLICATION_JSON

        return HttpEntity(body, headers)
    }

    private fun exchange(jsonEntity: HttpEntity<*>, endpointUrl: String, httpMethod: HttpMethod) {
        LOGGER.info("$httpMethod: $fullUrl")

        try {
            responseEntity = rest.template.exchange(endpointUrl, httpMethod, jsonEntity, String::class.java)
        } catch (e: Exception) {
            ScenarioManager.errorLog("$httpMethod FEILET! ($fullUrl)", e)

            responseEntity = if (e is HttpStatusCodeException) {
                ResponseEntity.status(e.statusCode).body<String>("${e.javaClass.simpleName}: ${e.message}")
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body<String>("${e.javaClass.simpleName}: ${e.message}")
            }

            if (CucumberTestRun.isNotSanityCheck) {
                throw e
            }
        }
    }
}

class ResttjenesteMedBaseUrl(val template: RestTemplate, val baseUrl: String)
internal class FullUrl(baseUrl: String, endpointUrl: String) {
    private val fullUrl: String = "$baseUrl$endpointUrl"

    override fun toString(): String {
        return fullUrl
    }
}
