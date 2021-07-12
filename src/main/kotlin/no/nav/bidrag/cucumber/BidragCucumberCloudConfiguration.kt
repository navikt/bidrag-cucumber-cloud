package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.model.IngressesAndTags
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
    fun ingressesAndTags() = IngressesAndTags()

    @Bean
    @Scope("prototype")
    fun restTemplate() = RestTemplate()
}
