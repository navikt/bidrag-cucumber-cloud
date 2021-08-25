package no.nav.bidrag.cucumber

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.cucumber.aop.ExceptionLoggerAspect
import no.nav.bidrag.cucumber.aop.TestFailedAdvice
import no.nav.bidrag.cucumber.hendelse.JournalpostKafkaHendelseProducer
import no.nav.bidrag.cucumber.model.SuppressStackTraceText
import no.nav.bidrag.cucumber.sikkerhet.TokenProvider
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Scope
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriTemplateHandler
import java.net.URI
import java.security.cert.X509Certificate

@Configuration
class SpringConfig {

    @Bean
    fun tokenProvider(restTemplate: RestTemplate) = TokenProvider(restTemplate)

    @Bean
    fun openAPI(): OpenAPI = OpenAPI().info(
        Info()
            .title("bidrag-cucumber-cloud")
            .description("Funksjonelle tester for nais applikasjoner som er sikret med azure ad og bruker rest/kafka")
            .version("v1")
    )

    @Bean
    fun suppressStackTraceText() = SuppressStackTraceText()

    @Bean
    fun correlationIdFilter() = CorrelationIdFilter()

    @Bean
    fun exceptionLogger() = ExceptionLogger(
        BidragCucumberCloud::class.java.simpleName, ExceptionLoggerAspect::class.java, TestFailedAdvice::class.java
    )

    @Bean
    fun httpComponentsClientHttpRequestFactorySomIgnorererHttps(): HttpComponentsClientHttpRequestFactory {
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

@Configuration
@Profile(PROFILE_LIVE)
class LiveSpringConfig {

    @Bean
    fun jornalpostKafkaHendelseProducer(
        kafkaTemplate: KafkaTemplate<String, String>,
        @Value("\${TOPIC_JOURNALPOST}") topic: String,
        objectMapper: ObjectMapper
    ) = JournalpostKafkaHendelseProducer(kafkaTemplate = kafkaTemplate, topic = topic, objectMapper = objectMapper)
}

@Configuration
class PrototypeSpringConfig {

    @Bean
    @Scope("prototype")
    fun httpHeaderRestTemplate(httpComponentsClientHttpRequestFactory: HttpComponentsClientHttpRequestFactory): HttpHeaderRestTemplate {
        return HttpHeaderRestTemplate(httpComponentsClientHttpRequestFactory)
    }
}
