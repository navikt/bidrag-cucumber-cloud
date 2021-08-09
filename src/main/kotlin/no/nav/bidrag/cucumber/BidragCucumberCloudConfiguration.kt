package no.nav.bidrag.cucumber

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.commons.web.CorrelationIdFilter
import no.nav.bidrag.cucumber.aop.ExceptionLoggerAspect
import no.nav.bidrag.cucumber.aop.TestFailedAdvice
import no.nav.bidrag.cucumber.model.SuppressStackTraceText
import no.nav.bidrag.cucumber.sikkerhet.TokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.web.client.RestTemplate

@Configuration
class BidragCucumberCloudConfiguration {

    @Bean
    fun tokenProvider(restTemplate: RestTemplate) = TokenProvider(restTemplate)

    @Bean
    @Scope("prototype")
    fun restTemplate() = RestTemplate()

    @Bean
    @Suppress("HasPlatformType")
    fun openAPI() = OpenAPI().info(
        Info().title("bidrag-cucumber-cloud").description("Funksjonelle tester for azure ad applikasjoner").version("v1")
    )

    @Bean
    fun suppressStackTraceText() = SuppressStackTraceText()

    @Bean
    fun correlationIdFilter() = CorrelationIdFilter()

    @Bean
    fun exceptionLogger() = ExceptionLogger(
        BidragCucumberCloud::class.java.simpleName, ExceptionLoggerAspect::class.java, TestFailedAdvice::class.java
    )
}
