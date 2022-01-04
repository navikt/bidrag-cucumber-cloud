package no.nav.bidrag.cucumber.sikkerhet

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository

@Configuration
class OAuth2ClientConfiguration {
    @Bean
    fun authorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository?,
        authorizedClientService: OAuth2AuthorizedClientService?
    ): OAuth2AuthorizedClientManager? {
        val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
            .clientCredentials()
            .build()
        val authorizedClientManager = AuthorizedClientServiceOAuth2AuthorizedClientManager(
            clientRegistrationRepository, authorizedClientService
        )
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
        return authorizedClientManager
    }
}