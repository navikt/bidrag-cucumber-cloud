package no.nav.bidrag.cucumber.service

import org.springframework.context.annotation.Lazy
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.stereotype.Service

@Service
class AzureTokenService(@Lazy val authorizedClientManager: OAuth2AuthorizedClientManager): TokenService() {

    private val ANONYMOUS_AUTHENTICATION: Authentication = AnonymousAuthenticationToken(
        "anonymous", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
    )

    override fun generateToken(application: String): String {
            val accessToken = authorizedClientManager
                .authorize(
                    OAuth2AuthorizeRequest
                        .withClientRegistrationId(application)
                        .principal(ANONYMOUS_AUTHENTICATION)
                        .build()
                )!!.accessToken

            return "Bearer "+accessToken.tokenValue
    }
}