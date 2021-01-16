package no.nav.bidrag.cucumber

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.HttpClient
import no.nav.bidrag.commons.CorrelationId
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
    fun hentResponseSomListe() = ObjectMapper().readValue(responseEntity.body, List::class.java) as List<Map<String, Any>>
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
        return initHttpHeadersWithCorrelationIdAndEnhet(null)
    }

    private fun initHttpHeadersWithCorrelationIdAndEnhet(enhet: String?): HttpHeaders {
        val headers = HttpHeaders()
        headers.add(CorrelationId.CORRELATION_ID_HEADER, BidragCucumberNais.getCorrelationIdForScenario())
        headers.add(X_ENHET_HEADER, enhet ?: "4802")

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

    fun exchangePut(endpointUrl: String, journalpostJson: String) {
        exchangePut(endpointUrl, journalpostJson, null)
    }

    fun exchangePut(endpointUrl: String, journalpostJson: String, enhet: String?) {
        val jsonEntity = httpEntity(endpointUrl, enhet, journalpostJson)
        exchange(jsonEntity, endpointUrl, HttpMethod.PUT)
    }

    fun exchangePost(endpointUrl: String, json: String, enhet: String?) {
        val jsonEntity = httpEntity(endpointUrl, enhet, json)
        exchange(jsonEntity, endpointUrl, HttpMethod.POST)
    }

    fun exchangePost(endpointUrl: String, json: String) {
        val jsonEntity = httpEntity(endpointUrl = endpointUrl, json = json, enhet = null)
        exchange(jsonEntity, endpointUrl, HttpMethod.POST)
    }

    fun exchangePost(endpointUrl: String) {
        val jsonEntity = httpEntity(endpointUrl)
        exchange(jsonEntity, endpointUrl, HttpMethod.POST)
    }

    internal fun httpEntity(endpointUrl: String): HttpEntity<String> {
        this.debugFullUrl = rest.baseUrl + endpointUrl
        val headers = initHttpHeadersWithCorrelationIdAndEnhet()
        headers.contentType = MediaType.APPLICATION_JSON

        return HttpEntity(headers)
    }

    private fun httpEntity(endpointUrl: String, enhet: String?, json: String): HttpEntity<String> {
        this.debugFullUrl = rest.baseUrl + endpointUrl
        val headers = initHttpHeadersWithCorrelationIdAndEnhet(enhet)
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

    fun post(endpointUrl: String, jsonEntity: HttpEntity<String>) {
        debugFullUrl = rest.baseUrl + endpointUrl

        responseEntity = try {
            rest.template.postForEntity(endpointUrl, jsonEntity, String::class.java)
        } catch (e: HttpStatusCodeException) {
            LOGGER.error("POST FEILET: $debugFullUrl: $e")
            ResponseEntity(e.statusCode)
        }
    }

    fun hentManglendeProperties(objects: List<*>, properties: List<String>): List<String> {
        val manglendeProps = ArrayList<String>()

        objects.forEach {
            @Suppress("UNCHECKED_CAST") manglendeProps.addAll(hentManglendeProperties(it as LinkedHashMap<String, *>, properties))
        }

        return manglendeProps
    }

    fun hentManglendeProperties(objects: LinkedHashMap<*, *>, properties: List<String>): List<String> {
        val manglendeProps = ArrayList<String>()
        properties.forEach { if (!objects.containsKey(it)) manglendeProps.add(it) }

        return manglendeProps
    }

    class ResttjenesteMedBaseUrl(val template: RestTemplate, val httpClient: HttpClient? = null, val baseUrl: String)
}
