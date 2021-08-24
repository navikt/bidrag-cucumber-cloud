package no.nav.bidrag.cucumber

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.cucumber.sikkerhet.Sikkerhet
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.util.UriTemplateHandler
import java.net.URI
import java.security.cert.X509Certificate

internal object RestTjenesteForApplikasjon {
    private val LOGGER = LoggerFactory.getLogger(RestTjenesteForApplikasjon::class.java)
    private val REST_TJENESTE_FOR_APPLIKASJON = RestTjenesteForApplikasjonThreadLocal()

    fun hentEllerKonfigurer(applicationName: String): RestTjeneste.ResttjenesteMedBaseUrl {
        return REST_TJENESTE_FOR_APPLIKASJON.hentEllerKonfigurer(applicationName) { konfigurer(applicationName) }
    }

    private fun konfigurer(applicationName: String): RestTjeneste.ResttjenesteMedBaseUrl {

        val ingress = Environment.fetchIngress(applicationName)
        val applicationUrl = joinIngressAndApplicationName(ingress, applicationName)

        return konfigurerSikkerhet(applicationName, applicationUrl)
    }

    private fun joinIngressAndApplicationName(ingress: String, applicationName: String): String {
        val ingressUrl = if (ingress.endsWith('/')) ingress.removeSuffix("/") else ingress

        return "$ingressUrl/$applicationName"
    }

    private fun konfigurerSikkerhet(applicationName: String, applicationUrl: String): RestTjeneste.ResttjenesteMedBaseUrl {

        val httpComponentsClientHttpRequestFactory = hentHttpRequestFactorySomIgnorererHttps()
        val httpHeaderRestTemplate = HttpHeaderRestTemplate(httpComponentsClientHttpRequestFactory)
        httpHeaderRestTemplate.uriTemplateHandler = BaseUrlTemplateHandler(applicationUrl)

        if (Environment.isNotSanityCheck() && Environment.isTestUserPresent()) {
            httpHeaderRestTemplate.addHeaderGenerator(HttpHeaders.AUTHORIZATION) { Sikkerhet.fetchAzureBearerToken() }
        } else {
            val message = if (Environment.isSanityCheck) {
                "No security provided when running sanity check on $applicationName"
            } else {
                "No user to provide security for when accessing $applicationName"
            }

            LOGGER.info(message)
        }

        return RestTjeneste.ResttjenesteMedBaseUrl(httpHeaderRestTemplate, applicationUrl)
    }

    private fun hentHttpRequestFactorySomIgnorererHttps(): HttpComponentsClientHttpRequestFactory {
        val acceptingTrustStrategy = { _: Array<X509Certificate>, _: String -> true }
        val sslContext = SSLContexts.custom()
            .loadTrustMaterial(null, acceptingTrustStrategy)
            .build()

        val csf = SSLConnectionSocketFactory(sslContext)

        val httpClient = HttpClients.custom()
            .setSSLSocketFactory(csf)
            .build()

        val requestFactory = HttpComponentsClientHttpRequestFactory()

        requestFactory.httpClient = httpClient

        return requestFactory
    }

    private class BaseUrlTemplateHandler(val baseUrl: String) : UriTemplateHandler {
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

    class RestTjenesteForApplikasjonThreadLocal {
        companion object {
            @JvmStatic
            private val REST_TJENESTER = ThreadLocal<MutableMap<String, RestTjeneste.ResttjenesteMedBaseUrl>>()
        }

        fun hentEllerKonfigurer(applicationName: String, konfigurer: () -> RestTjeneste.ResttjenesteMedBaseUrl): RestTjeneste.ResttjenesteMedBaseUrl {
            val tradensResttjenester = hentEllerLag()
            return tradensResttjenester.computeIfAbsent(applicationName) { konfigurer() }
        }

        private fun hentEllerLag(): MutableMap<String, RestTjeneste.ResttjenesteMedBaseUrl> {
            if (REST_TJENESTER.get() == null) {
                REST_TJENESTER.set(HashMap())
            }

            return REST_TJENESTER.get()
        }
    }
}
