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
    internal var responseEntity: ResponseEntity<String?>? = null

    constructor(naisApplication: String) : this(RestTjenesteForApplikasjon.hentEllerKonfigurer(naisApplication))

    fun hentFullUrlMedEventuellWarning() = "$fullUrl${appendWarningWhenExists()}"
    fun hentHttpStatus(): HttpStatus = responseEntity?.statusCode ?: HttpStatus.I_AM_A_TEAPOT
    fun hentResponse(): String? = responseEntity?.body
    fun hentResponseSomMap() = if (responseEntity?.statusCode == HttpStatus.OK && responseEntity?.body != null)
        ObjectMapper().readValue(responseEntity!!.body, Map::class.java) as Map<String, Any>
    else
        HashMap()

    private fun appendWarningWhenExists(): String {
        val warnings = responseEntity?.headers?.get(HttpHeaders.WARNING) ?: emptyList()

        return if (warnings.isNotEmpty()) " - ${warnings[0]}" else ""
    }

    fun exchangeGet(endpointUrl: String): ResponseEntity<String?> {
        fullUrl = FullUrl(rest.baseUrl, endpointUrl)

        val header = initHttpHeadersWithCorrelationIdAndEnhet()

        exchange(HttpEntity(null, header), endpointUrl, HttpMethod.GET)

        ScenarioManager.log(
            if (responseEntity?.body != null) "response with body and status ${responseEntity!!.statusCode}"
            else if (responseEntity == null) "no response entity" else "no response body with status ${responseEntity!!.statusCode}"
        )

        return responseEntity ?: ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build()
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

    fun exchangePatch(endpointUrl: String, json: String) {
        val jsonEntity = httpEntity(endpointUrl, json)
        exchange(jsonEntity, endpointUrl, HttpMethod.PATCH)
    }

    private fun httpEntity(endpointUrl: String, json: String): HttpEntity<String> {
        this.fullUrl = FullUrl(rest.baseUrl, endpointUrl)
        val headers = initHttpHeadersWithCorrelationIdAndEnhet()
        headers.contentType = MediaType.APPLICATION_JSON

        return HttpEntity(json, headers)
    }

    private fun exchange(jsonEntity: HttpEntity<String>, endpointUrl: String, httpMethod: HttpMethod) {
        ScenarioManager.log("$httpMethod: $fullUrl")

        if (Environment.isTestUserPresent()) {
            try {
                responseEntity = rest.template.exchange(endpointUrl, httpMethod, jsonEntity, String::class.java)
            } catch (e: Exception) {
                ScenarioManager.errorLog("$httpMethod FEILET! ($fullUrl) - $e", e)

                if (e is HttpStatusCodeException) {
                    responseEntity = ResponseEntity.status(e.statusCode).body<String>("${e.javaClass.simpleName}: ${e.message}")
                } else {
                    responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body<String>("${e.javaClass.simpleName}: ${e.message}")
                }

                if (Environment.isNotSanityCheck()) {
                    throw e
                }
            }
        } else {
            ScenarioManager.log("Ikke nødvendig å kontakte endpoint uten testbruker ved sanity check!")
        }
    }

    class ResttjenesteMedBaseUrl(val template: RestTemplate, val baseUrl: String)
    internal class FullUrl(baseUrl: String, endpointUrl: String) {
        private val fullUrl: String = "$baseUrl$endpointUrl"

        override fun toString(): String {
            return fullUrl
        }
    }
}
