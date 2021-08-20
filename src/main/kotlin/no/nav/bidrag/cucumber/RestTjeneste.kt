package no.nav.bidrag.cucumber

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.commons.web.EnhetFilter.X_ENHET_HEADER
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

@Suppress("UNCHECKED_CAST")
open class RestTjeneste(
    internal val rest: ResttjenesteMedBaseUrl
) {
    private lateinit var fullUrl: FullUrl
    private lateinit var responseEntity: ResponseEntity<String?>

    constructor(naisApplication: String) : this(RestTjenesteForApplikasjon.hentEllerKonfigurer(naisApplication))

    fun hentFullUrlMedEventuellWarning() = "${fullUrl.get()}${appendWarningWhenExists()}"
    fun hentHttpStatus(): HttpStatus = responseEntity.statusCode
    fun hentResponse(): String? = responseEntity.body
    fun hentResponseSomMap() = if (responseEntity.statusCode == HttpStatus.OK && responseEntity.body != null)
        ObjectMapper().readValue(responseEntity.body, Map::class.java) as Map<String, Any>
    else
        HashMap()

    private fun appendWarningWhenExists(): String {
        val warnings = responseEntity.headers[HttpHeaders.WARNING] ?: emptyList()

        return if (warnings.isNotEmpty()) " - ${warnings.get(0)}" else ""
    }

    fun exchangeGet(endpointUrl: String): ResponseEntity<String?> {
        fullUrl = FullUrl(rest.baseUrl, endpointUrl)

        val header = initHttpHeadersWithCorrelationIdAndEnhet()

        exchange(HttpEntity(null, header), endpointUrl, HttpMethod.GET)

        ScenarioManager.log(
            if (responseEntity.body != null) "response with json and status ${responseEntity.statusCode}"
            else "no response body with status ${responseEntity.statusCode}"
        )

        return responseEntity
    }

    private fun initHttpHeadersWithCorrelationIdAndEnhet(): HttpHeaders {
        val headers = HttpHeaders()
        headers.add(CorrelationId.CORRELATION_ID_HEADER, ScenarioManager.getCorrelationIdForScenario())
        headers.add(X_ENHET_HEADER, "4802")

        ScenarioManager.log(
            ScenarioManager.createCorrelationIdLinkTitle(),
            ScenarioManager.createQueryLinkForCorrelationId()
        )

        return headers
    }

    fun exchangePost(endpointUrl: String, json: String) {
        val jsonEntity = httpEntity(endpointUrl, json)
        exchange(jsonEntity, endpointUrl, HttpMethod.POST)
    }

    private fun httpEntity(endpointUrl: String, json: String): HttpEntity<String> {
        this.fullUrl = FullUrl(rest.baseUrl, endpointUrl)
        val headers = initHttpHeadersWithCorrelationIdAndEnhet()
        headers.contentType = MediaType.APPLICATION_JSON

        return HttpEntity(json, headers)
    }

    private fun exchange(jsonEntity: HttpEntity<String>, endpointUrl: String, httpMethod: HttpMethod) {
        try {
            ScenarioManager.log("$httpMethod: $fullUrl")
            responseEntity = rest.template.exchange(endpointUrl, httpMethod, jsonEntity, String::class.java)
        } catch (e: HttpStatusCodeException) {
            ScenarioManager.errorLog("$httpMethod FEILET: $fullUrl: $e")
            responseEntity = ResponseEntity.status(e.statusCode).body<Any>("${e.javaClass.simpleName}: ${e.message}") as ResponseEntity<String?>

            if (Environment.isNotSanityCheck()) {
                throw e
            }
        }
    }

    class ResttjenesteMedBaseUrl(val template: RestTemplate, val baseUrl: String)
    internal data class FullUrl(val baseUrl: String, val endpointUrl: String) {
        private val fullUrl: String

        init {
            val base = if (baseUrl.endsWith('/')) baseUrl.removeSuffix("/") else baseUrl
            val enpoint = if (endpointUrl.startsWith('/')) endpointUrl.removePrefix("/") else endpointUrl

            fullUrl = "$base/$enpoint"
        }

        fun get() = fullUrl

        override fun toString(): String {
            return fullUrl
        }
    }
}
