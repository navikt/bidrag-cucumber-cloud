package no.nav.bidrag.cucumber

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import java.security.cert.X509Certificate

private val LOGGER = LoggerFactory.getLogger(CacheRestTemplateMedBaseUrl::class.java)

internal object CacheRestTemplateMedBaseUrl {
    private val restTjenesteTilApplikasjon: MutableMap<String, RestTjeneste.RestTemplateMedBaseUrl> = HashMap()
    private val naisConfiguration = NaisConfiguration()

    fun hentEllerKonfigurer(applicationName: String): RestTjeneste.RestTemplateMedBaseUrl {

        if (restTjenesteTilApplikasjon.containsKey(applicationName)) {
            return restTjenesteTilApplikasjon.getValue(applicationName)
        }

        naisConfiguration.read(applicationName)
        val applicationHostUrl = naisConfiguration.hentApplicationHostUrl(applicationName)
        val applicationUrl: String

        if (!applicationHostUrl.endsWith('/') && !applicationName.startsWith('/')) {
            applicationUrl = "$applicationHostUrl/$applicationName/"
        } else {
            applicationUrl = "$applicationHostUrl$applicationName/"
        }

        return hentEllerKonfigurerApplikasjonForUrl(applicationName, applicationUrl)
    }

    private fun hentEllerKonfigurerApplikasjonForUrl(applicationName: String, applicationUrl: String): RestTjeneste.RestTemplateMedBaseUrl {

        val httpComponentsClientHttpRequestFactory = hentHttpRequestFactorySomIgnorererHttps()
        val httpHeaderRestTemplate = Environment.setBaseUrlPa(HttpHeaderRestTemplate(httpComponentsClientHttpRequestFactory), applicationUrl)

        when (Sikkerhet.SECURITY_FOR_APPLICATION[applicationName]) {
            Security.AZURE -> httpHeaderRestTemplate.addHeaderGenerator(HttpHeaders.AUTHORIZATION) { Sikkerhet.fetchIdToken() }
            Security.NONE -> LOGGER.info("No security needed when accessing $applicationName")
        }

        restTjenesteTilApplikasjon[applicationName] = RestTjeneste.RestTemplateMedBaseUrl(httpHeaderRestTemplate, applicationUrl)

        return restTjenesteTilApplikasjon[applicationName]!!
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
}
