package no.nav.bidrag.cucumber

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.cucumber.aop.ExceptionLoggerAspect
import no.nav.bidrag.cucumber.aop.TestFailedAdvice
import no.nav.bidrag.cucumber.hendelse.JournalpostKafkaHendelseProducer
import no.nav.bidrag.cucumber.model.SuppressStackTraceText
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Scope
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.kafka.core.KafkaTemplate

@Configuration
@OpenAPIDefinition(
    info = io.swagger.v3.oas.annotations.info.Info(
        title = "bidrag-cucumber-cloud",
        description = "Funksjonelle tester for nais applikasjoner som er sikret med azure ad og bruker rest/kafka",
        version = "v1"
    ),
    security = [SecurityRequirement(name = "basicAuth")]
)
@SecurityScheme(
    name = "basicAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "basic"
)
class SpringConfig {

    @Bean
    fun suppressStackTraceText() = SuppressStackTraceText()

    @Bean
    fun correlationIdFilter() = CorrelationIdFilter()

    @Bean
    fun exceptionLogger() = ExceptionLogger(
        BidragCucumberCloud::class.java.simpleName,
        ExceptionLoggerAspect::class.java,
        TestFailedAdvice::class.java
    )

    @Bean
    @Scope("prototype")
    fun httpHeaderRestTemplate(): HttpHeaderRestTemplate {
        return HttpHeaderRestTemplate(HttpComponentsClientHttpRequestFactory())
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
