package no.nav.bidrag.cucumber

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestConfiguration {
    @Bean
    fun restTemplateBuilder(
        @Value("\${REST_AUTH_BRUKERNAVN}") authBrukernavn: String,
        @Value("\${REST_AUTH_PASSORD}") authPassord: String
    ): RestTemplateBuilder {
        return RestTemplateBuilder().basicAuthentication(authBrukernavn, authPassord)
    }
}
