package no.nav.bidrag.cucumber

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.cucumber.sikkerhet.Sikkerhet
import no.nav.bidrag.cucumber.sikkerhet.Sikkerhet.Security
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.util.UriTemplateHandler
import java.net.URI
import java.security.cert.X509Certificate

internal object CacheRestTemplateMedBaseUrl {
    private val LOGGER = LoggerFactory.getLogger(CacheRestTemplateMedBaseUrl::class.java)
    private val REST_TJENESTE_TIL_APPLIKASJON: MutableMap<String, RestTjeneste.ResttjenesteMedBaseUrl> = HashMap()

    fun hentEllerKonfigurer(applicationName: String): RestTjeneste.ResttjenesteMedBaseUrl {
        return REST_TJENESTE_TIL_APPLIKASJON.computeIfAbsent(applicationName) { konfigurer(applicationName) }
    }

    private fun konfigurer(applicationName: String): RestTjeneste.ResttjenesteMedBaseUrl {

        NaisConfiguration.read(applicationName)

        val applicationHostUrl = NaisConfiguration.hentApplicationHostUrl(applicationName)

        val applicationUrl = if (!applicationHostUrl.endsWith('/') && !applicationName.startsWith('/')) {
            "$applicationHostUrl/$applicationName/"
        } else {
            "$applicationHostUrl$applicationName/"
        }

        return konfigurerSikkerhet(applicationName, applicationUrl)
    }

    private fun konfigurerSikkerhet(applicationName: String, applicationUrl: String): RestTjeneste.ResttjenesteMedBaseUrl {

        val httpComponentsClientHttpRequestFactory = hentHttpRequestFactorySomIgnorererHttps()
        val httpHeaderRestTemplate = HttpHeaderRestTemplate(httpComponentsClientHttpRequestFactory)
        httpHeaderRestTemplate.uriTemplateHandler = BaseUrlTemplateHandler(applicationUrl)

        when (Sikkerhet.fetchSecurityFor(applicationName)) {
            Security.AZURE -> httpHeaderRestTemplate.addHeaderGenerator(HttpHeaders.AUTHORIZATION) { Sikkerhet.fetchAzureToken(applicationName) }
            Security.NONE -> LOGGER.info("No security needed when accessing $applicationName")
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
}
