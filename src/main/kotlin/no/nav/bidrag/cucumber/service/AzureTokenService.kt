package no.nav.bidrag.cucumber.service

import com.github.benmanes.caffeine.cache.Cache
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant
import com.nimbusds.oauth2.sdk.ErrorObject
import com.nimbusds.oauth2.sdk.JWTBearerGrant
import com.nimbusds.oauth2.sdk.ResourceOwnerPasswordCredentialsGrant
import com.nimbusds.oauth2.sdk.Scope
import com.nimbusds.oauth2.sdk.TokenRequest
import com.nimbusds.oauth2.sdk.TokenResponse
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost
import com.nimbusds.oauth2.sdk.auth.Secret
import com.nimbusds.oauth2.sdk.http.HTTPRequest
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.token.Tokens
import no.nav.bidrag.cucumber.AzureTokenException
import no.nav.bidrag.cucumber.dto.SaksbehandlerType
import no.nav.bidrag.cucumber.usernameNotFound
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.net.URI


data class AzureTokenRequest(
    val app: String,
    val saksbehandlerType: SaksbehandlerType? = null,
)

@Component
@ConfigurationProperties("appscope")
class AppScopes {
    lateinit var clients: Map<String, String>
}

@Component
@ConfigurationProperties("testbrukere")
class TestBrukere {
    lateinit var identer: Map<String, String>
}

@Service
class AzureTokenService(
    @Value("\${user_password}") val user_password: String,
    val testusers: TestBrukere,
    val scopes: AppScopes,
    @Value("\${AZURE_OPENID_CONFIG_TOKEN_ENDPOINT}") val tokenUri: String,
    @Value("\${AZURE_APP_CLIENT_ID}") val clientId: String,
    @Value("\${AZURE_APP_CLIENT_SECRET}") val clientSecret: String
): TokenService() {

    private lateinit var tokenCache: Cache<AzureTokenRequest, Tokens>
    private lateinit var clientAuth: ClientSecretPost

    init {
        val clientID = ClientID(clientId)
        val clientSecret = Secret(clientSecret)
        clientAuth = ClientSecretPost(clientID, clientSecret)
        tokenCache = AzureTokenCache.accessTokenResponseCache(1000, 10)
    }

    override fun generateToken(application: String, saksbehandlerType: SaksbehandlerType?): String {
        val request = AzureTokenRequest(application, saksbehandlerType)
        return tokenCache.get(request) { s: AzureTokenRequest? -> generateToken(request) }!!.accessToken.value
    }

    fun generateToken(request: AzureTokenRequest): Tokens {
        if (request.saksbehandlerType != null){
            return generateOnBehalfOfToken(request.app, request.saksbehandlerType)
        }
        return generateClientCredentialsToken(request.app)
    }
    private fun generateOnBehalfOfToken(appName: String, saksbehandlerType: SaksbehandlerType): Tokens {
        val appScope = scopes.clients[appName]
        val userId = testusers.identer[saksbehandlerType.name] ?: usernameNotFound()
        val usertoken = getUserToken(userId)
        LOGGER.trace("Henter ny Azure on-behalf-of token for scope {}", appScope)
        return try {
            val scope = Scope(appScope)
            val tokenEndpoint = URI(tokenUri)
            val customParams: MutableMap<String, List<String>> = HashMap()
            customParams["requested_token_use"] = listOf("on_behalf_of")
            customParams["client_assertion_type"] = listOf("urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
            val request = TokenRequest(tokenEndpoint, clientAuth, JWTBearerGrant(SignedJWT.parse(usertoken.accessToken.value)), scope, null, customParams)
            doTokenRequest(request)
        } catch (e: Exception) {
            LOGGER.error("Det skjedde en feil ved henting av on-behalf-of token fra Azure", e)
            throw AzureTokenException("Det skjedde en feil ved henting av token fra Azure", e)
        }
    }

    private fun generateClientCredentialsToken(appName: String): Tokens {
        val appScope = scopes.clients[appName]
        LOGGER.trace("Henter ny Azure client-credentials grant token for scope {}", appScope)
        return try {
            val scope = Scope(appScope)
            val tokenEndpoint = URI(tokenUri)
            val request = TokenRequest(tokenEndpoint, clientAuth, ClientCredentialsGrant(), scope)
            doTokenRequest(request)
        } catch (e: Exception) {
            LOGGER.error("Det skjedde en feil ved henting av client-credentials token fra Azure", e)
            throw AzureTokenException("Det skjedde en feil ved henting av token fra Azure", e)
        }
    }

    fun getUserToken(userId: String): Tokens {
        return try {
            val scope = Scope("openid offline_access $clientId/.default")
            val tokenEndpoint = URI(tokenUri)
            val request = TokenRequest(tokenEndpoint, clientAuth, ResourceOwnerPasswordCredentialsGrant("f_$userId.e_$userId@trygdeetaten.no", Secret(user_password)), scope)
            doTokenRequest(request)
        } catch (e: Exception) {
            LOGGER.error("Det skjedde en feil ved henting av client-credentials token fra Azure", e)
            throw AzureTokenException("Det skjedde en feil ved henting av token fra Azure", e)
        }
    }

    private fun doTokenRequest(request: TokenRequest): Tokens {
        return try {
            val httpRequest: HTTPRequest = request.toHTTPRequest()
            val response: TokenResponse = TokenResponse.parse(httpRequest.send())
            if (!response.indicatesSuccess()) {
                // We got an error response...
                val errorObject: ErrorObject = response.toErrorResponse().errorObject
                val errorMessage = String.format(
                    "Det skjedde en feil ved henting av token fra Azure - code: %s, description: %s, uri: %s, statusCode: %s",
                    errorObject.code,
                    errorObject.description,
                    errorObject.uri,
                    errorObject.httpStatusCode
                )
                LOGGER.error(errorMessage)
                throw AzureTokenException(errorMessage)
            }
            response.toSuccessResponse().tokens
        } catch (e: Exception) {
            LOGGER.error("Det skjedde en feil ved henting av token fra Azure", e)
            throw AzureTokenException("Det skjedde en feil ved henting av token fra Azure", e)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AzureTokenService::class.java)
    }
}