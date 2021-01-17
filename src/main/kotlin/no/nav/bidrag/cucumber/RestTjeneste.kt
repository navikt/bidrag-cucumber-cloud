package no.nav.bidrag.cucumber

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.commons.web.EnhetFilter.X_ENHET_HEADER
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

private val LOGGER = LoggerFactory.getLogger(RestTjeneste::class.java)

@Suppress("UNCHECKED_CAST")
open class RestTjeneste(
    private val applicationName: String,
    private val rest: ResttjenesteMedBaseUrl
) {

    private lateinit var debugFullUrl: String
    private lateinit var responseEntity: ResponseEntity<String>

    constructor(naisApplication: String) : this(naisApplication, CacheRestTemplateMedBaseUrl.hentEllerKonfigurer(naisApplication))

    fun hentEndpointUrl() = debugFullUrl
    fun hentHttpHeaders(): HttpHeaders = responseEntity.headers
    fun hentHttpStatus(): HttpStatus = responseEntity.statusCode
    fun hentResponse(): String? = responseEntity.body
    fun hentResponseSomMap() = ObjectMapper().readValue(responseEntity.body, Map::class.java) as Map<String, Any>

    fun exchangeGet(endpointUrl: String): ResponseEntity<String> {
        debugFullUrl = rest.baseUrl + endpointUrl

        val header = initHttpHeadersWithCorrelationIdAndEnhet()

        BidragCucumberNais.log("GET ${this.debugFullUrl}")

        responseEntity = try {
            rest.template.exchange(endpointUrl, HttpMethod.GET, HttpEntity(null, header), String::class.java)
        } catch (e: HttpStatusCodeException) {
            ResponseEntity(headerWithAlias(), e.statusCode)
        }

        BidragCucumberNais.log(
            if (responseEntity.body != null) "response with json and status ${responseEntity.statusCode}"
            else "no response body with status ${responseEntity.statusCode}"
        )

        return responseEntity
    }

    internal fun initHttpHeadersWithCorrelationIdAndEnhet(): HttpHeaders {
        val headers = HttpHeaders()
        headers.add(CorrelationId.CORRELATION_ID_HEADER, BidragCucumberNais.getCorrelationIdForScenario())
        headers.add(X_ENHET_HEADER, "4802")

        BidragCucumberNais.log(
            BidragCucumberNais.createCorrelationIdLinkTitle(),
            BidragCucumberNais.createQueryLinkForCorrelationId()
        )

        return headers
    }

    private fun headerWithAlias(): MultiValueMap<String, String> {
        val httpHeaders = HttpHeaders()
        httpHeaders.add("ERROR_REST_SERVICE", applicationName)

        return httpHeaders
    }

    fun exchangePost(endpointUrl: String, json: String) {
        val jsonEntity = httpEntity(endpointUrl, json)
        exchange(jsonEntity, endpointUrl, HttpMethod.POST)
    }

    internal fun httpEntity(endpointUrl: String, json: String): HttpEntity<String> {
        this.debugFullUrl = rest.baseUrl + endpointUrl
        val headers = initHttpHeadersWithCorrelationIdAndEnhet()
        headers.contentType = MediaType.APPLICATION_JSON

        return HttpEntity(json, headers)
    }

    internal fun exchange(jsonEntity: HttpEntity<String>, endpointUrl: String, httpMethod: HttpMethod) {
        try {
            LOGGER.info("$httpMethod: $endpointUrl")
            responseEntity = rest.template.exchange(endpointUrl, httpMethod, jsonEntity, String::class.java)
        } catch (e: HttpStatusCodeException) {
            LOGGER.error("$httpMethod FEILET: $debugFullUrl: $e")
            responseEntity = ResponseEntity.status(e.statusCode).body<Any>("${e.javaClass.simpleName}: ${e.message}") as ResponseEntity<String>
            throw e
        }
    }

    fun removeHeaderGenerator(headerName: String) {
        val restTjeneste = rest.template

        if (restTjeneste is HttpHeaderRestTemplate) {
            restTjeneste.removeHeaderGenerator(headerName)
        } else {
            throw IllegalStateException("Ukjent implementasjon for bidrag")
        }
    }

    class ResttjenesteMedBaseUrl(val template: RestTemplate, val baseUrl: String)
}
