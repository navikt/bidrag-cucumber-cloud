package no.nav.bidrag.cucumber

import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger(Sikkerhet::class.java)

object Sikkerhet {

    internal val SECURITY_FOR_APPLICATION: MutableMap<String, Security> = HashMap()
    private lateinit var onlineToken: String
    private val finalValueCache: MutableMap<String, Any> = HashMap()

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

    fun fetchOnlineIdToken(): String {
//        finalValueCache[OPEN_ID_FASIT] = finalValueCache[OPEN_ID_FASIT] ?: hentOpenIdConnectFasitRessurs(namespace)
//        finalValueCache[OPEN_AM_PASSWORD] = finalValueCache[OPEN_AM_PASSWORD] ?: hentOpenAmPwd(finalValueCache[OPEN_ID_FASIT] as Fasit.FasitRessurs)
//        finalValueCache[TEST_USER_AUTH_TOKEN] = finalValueCache[TEST_USER_AUTH_TOKEN] ?: hentTokenIdForTestbruker()
//        val codeFraLocationHeader = hentCodeFraLocationHeader(finalValueCache[TEST_USER_AUTH_TOKEN] as String)
//
//        LOGGER.info("Fetched id token for ${Environment.testUser()}")

        return "Bearer todo:token"
    }
}

enum class Security {
    AZURE, NONE
}