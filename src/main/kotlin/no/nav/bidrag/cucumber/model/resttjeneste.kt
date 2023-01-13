package no.nav.bidrag.cucumber.model

import no.nav.bidrag.commons.CorrelationId
import no.nav.bidrag.commons.web.EnhetFilter
import no.nav.bidrag.cucumber.ScenarioManager
import no.nav.bidrag.cucumber.dto.SaksbehandlerType
import no.nav.bidrag.cucumber.service.AzureTokenService
import no.nav.bidrag.cucumber.service.TokenService
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

internal class RestTjenester {
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

    private val restTjenesteForNavn: MutableMap<String, RestTjeneste> = HashMap()
    private var restTjenesteTilTesting: RestTjeneste? = null

    fun isApplicationConfigured(applicationName: String) = restTjenesteForNavn.contains(applicationName)
    fun hentRestTjenesteTilTesting() = restTjenesteTilTesting ?: throw IllegalStateException("RestTjeneste til testing er null!")
    fun hentRestTjeneste(applicationName: String) = restTjenesteForNavn[applicationName] ?: throw IllegalStateException(
        "RestTjeneste $applicationName er ikke funnet!"
    )

    fun settOppNaisApp(naisApplikasjon: String): RestTjeneste {
        LOGGER.info("Setter opp $naisApplikasjon")

        val restTjeneste: RestTjeneste

        if (!restTjenesteForNavn.contains(naisApplikasjon)) {
            restTjeneste = RestTjeneste.konfigurerResttjeneste(naisApplikasjon)
            restTjenesteForNavn[naisApplikasjon] = restTjeneste
        } else {
            restTjeneste = restTjenesteForNavn[naisApplikasjon]!!
        }

        return restTjeneste
    }

    fun settOppNaisAppTilTesting(naisApplikasjon: String) {
        restTjenesteTilTesting = settOppNaisApp(naisApplikasjon)
    }
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

        internal fun konfigurerResttjeneste(applicationName: String): RestTjeneste {
            if (CucumberTestRun.isApplicationConfigured(applicationName)) {
                return CucumberTestRun.hentRestTjenste(applicationName)
            }

            try {
                val applicationUrl = RestTjenester.konfigurerApplikasjonUrlFor(applicationName)
                val httpHeaderRestTemplate = BidragCucumberSingletons.hentPrototypeFraApplicationContext()
                httpHeaderRestTemplate.uriTemplateHandler = BaseUrlTemplateHandler(applicationUrl)

                if (!CucumberTestRun.skipAuth){
                    val tokenValue = hentToken(applicationName, CucumberTestRun.saksbehandlerType)
                    httpHeaderRestTemplate.addHeaderGenerator(HttpHeaders.AUTHORIZATION) { tokenValue.initBearerToken() }
                }

                return RestTjeneste(ResttjenesteMedBaseUrl(httpHeaderRestTemplate, applicationUrl))
            } catch (throwable: Throwable) {
                CucumberTestRun.holdExceptionForTest(throwable)

                throw throwable
            }
        }

        private fun hentToken(applicationName: String, saksbehandlerType: SaksbehandlerType? = null): TokenValue {
            val tokenService: TokenService = BidragCucumberSingletons.hentEllerInit(AzureTokenService::class) ?: throw notNullTokenService()

            return TokenValue(tokenService.getToken(applicationName, saksbehandlerType))
        }

        private fun notNullTokenService() = IllegalStateException("No token service in spring context")
    }

    private lateinit var fullUrl: FullUrl
    internal var responseEntity: ResponseEntity<String?>? = null

    fun hentFullUrlMedEventuellWarning() = "$fullUrl${appendWarningWhenExists()}"
    fun hentHttpStatus(): HttpStatus = responseEntity?.statusCode ?: HttpStatus.I_AM_A_TEAPOT
    fun hentResponse(): String? = responseEntity?.body
    fun hentResponseSomMap() = BidragCucumberSingletons.mapResponseSomMap(responseEntity)

    private fun appendWarningWhenExists(): String {
        val warnings = responseEntity?.headers?.get(HttpHeaders.WARNING) ?: emptyList()

        return if (warnings.isNotEmpty()) " - ${warnings[0]}" else ""
    }

    fun exchangeGet(endpointUrl: String, failOnNotFound: Boolean = true): ResponseEntity<String?> {

        val header = initHttpHeadersWithCorrelationIdAndEnhet()

        exchange(
            jsonEntity = HttpEntity(null, header),
            endpointUrl = endpointUrl,
            httpMethod = HttpMethod.GET,
            failOnNotFound = failOnNotFound
        )

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
        headers.add(CorrelationId.CORRELATION_ID_HEADER, ScenarioManager.fetchCorrelationIdForScenario())
        headers.add(EnhetFilter.X_ENHET_HEADER, "4802")

        return headers
    }

    fun exchangePost(endpointUrl: String, body: Any) {
        val jsonEntity = httpEntity(BidragCucumberSingletons.toJson(body))
        exchange(jsonEntity, endpointUrl, HttpMethod.POST)
    }

    fun exchangePost(endpointUrl: String, body: String) {
        val jsonEntity = httpEntity(body)
        exchange(jsonEntity, endpointUrl, HttpMethod.POST)
    }

    fun exchangePost(endpointUrl: String) {
        exchange(httpEntity("{}"), endpointUrl, HttpMethod.POST)
    }

    fun exchangePatch(endpointUrl: String, body: Any) {
        val jsonEntity = httpEntity(BidragCucumberSingletons.toJson(body))
        exchange(jsonEntity, endpointUrl, HttpMethod.PATCH)
    }

    private fun httpEntity(body: String): HttpEntity<String> {
        val headers = initHttpHeadersWithCorrelationIdAndEnhet()
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(body, headers)
    }

    private fun exchange(jsonEntity: HttpEntity<*>, endpointUrl: String, httpMethod: HttpMethod, failOnNotFound: Boolean = true) {
        fullUrl = FullUrl(rest.baseUrl, endpointUrl)
        LOGGER.info("$httpMethod: $fullUrl")

        try {
            responseEntity = rest.template.exchange(endpointUrl, httpMethod, jsonEntity, String::class.java)
        } catch (e: Exception) {
            responseEntity = if (e is HttpStatusCodeException) {
                ResponseEntity.status(e.statusCode).body<String>(failure(jsonEntity.body, e))
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body<String>(failure(jsonEntity.body, e))
            }

            if (isError(e, failOnNotFound)) {
                ScenarioManager.errorLog(">>> $httpMethod FEILET! ($fullUrl) ${failure(jsonEntity.body, e)}", e)

                if (CucumberTestRun.isNotSanityCheck) {
                    throw e
                }
            }
        }
    }

    private fun isError(e: Exception, failOn404: Boolean) = if (isNotFound(e)) failOn404 else true
    private fun isNotFound(e: Exception) = e is HttpStatusCodeException && e.statusCode == HttpStatus.NOT_FOUND
    private fun failure(body: Any?, e: Exception) = """-
    - input body: $body
    - exception : "${e::class.simpleName}: ${e.message}"
    """.trimIndent()
}

class ResttjenesteMedBaseUrl(val template: RestTemplate, val baseUrl: String)
class TokenValue(private val token: String) {
    fun initBearerToken() = "Bearer $token"
}

internal class FullUrl(baseUrl: String, endpointUrl: String) {
    private val fullUrl: String = "$baseUrl$endpointUrl"

    override fun toString(): String {
        return fullUrl
    }
}
