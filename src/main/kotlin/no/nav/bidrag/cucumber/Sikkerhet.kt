package no.nav.bidrag.cucumber

import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets

private val LOGGER = LoggerFactory.getLogger(Sikkerhet::class.java)

class Sikkerhet {

    companion object {
        private lateinit var onlineToken: String
        private val finalValueCache: MutableMap<String, Any> = HashMap()
    }

    internal fun fetchIdToken(): String {
        try {
            onlineToken = fetchOnlineIdToken()
            return onlineToken
        } catch (e: RuntimeException) {
            val exception = "${e.javaClass.name}: ${e.message} - ${e.stackTrace.first { it.fileName != null && it.fileName!!.endsWith("kt") }}"
            LOGGER.error("Feil ved henting av online id token, $exception")
            throw e
        }
    }

    private fun fetchOnlineIdToken(): String {
        return fetchOnlineIdToken(Environment.namespace)
    }

    fun fetchOnlineIdToken(namespace: String): String {
//        finalValueCache[OPEN_ID_FASIT] = finalValueCache[OPEN_ID_FASIT] ?: hentOpenIdConnectFasitRessurs(namespace)
//        finalValueCache[OPEN_AM_PASSWORD] = finalValueCache[OPEN_AM_PASSWORD] ?: hentOpenAmPwd(finalValueCache[OPEN_ID_FASIT] as Fasit.FasitRessurs)
//        finalValueCache[TEST_USER_AUTH_TOKEN] = finalValueCache[TEST_USER_AUTH_TOKEN] ?: hentTokenIdForTestbruker()
//        val codeFraLocationHeader = hentCodeFraLocationHeader(finalValueCache[TEST_USER_AUTH_TOKEN] as String)
//
//        LOGGER.info("Fetched id token for ${Environment.testUser()}")

        return "Bearer todo:token for $namespace"
    }

    internal fun base64EncodeCredentials(username: String, password: String): String {
        val credentials = "$username:$password"

        val encodedCredentials: ByteArray = java.util.Base64.getEncoder().encode(credentials.toByteArray())

        return String(encodedCredentials, StandardCharsets.UTF_8)
    }
}

enum class SecurityToken {
    AZURE, NONE
}