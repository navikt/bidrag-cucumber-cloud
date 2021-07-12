package no.nav.bidrag.cucumber.model

import no.nav.bidrag.cucumber.Environment
import org.slf4j.LoggerFactory

class IngressesAndTags {
    internal var value: String = ""
        set(value) {
            assertNotBlank(value)
            field = value
        }

    companion object {
        const val NOT_IGNORED = "not @ignored"
        private val INGRESS_FOR_APPLICATION: MutableMap<String, String> = HashMap()
        private val LOGGER = LoggerFactory.getLogger(IngressesAndTags::class.java)
    }

    constructor()
    constructor(ingressString: String) {
        value = ingressString
    }

    private fun assertNotBlank(value: String) {
        if (value.isBlank()) {
            val message = "Ingen ingress(er) med tag(s)!"
            LOGGER.error(message)
            throw IllegalStateException(message)
        }
    }


    fun fetchTags(): String {
        if (value.isBlank()) {
            value = Environment.ingressesForTags
        }

        val tagstring = value.split(',')
            .joinToString(prefix = "(", postfix = " and $NOT_IGNORED)", separator = " and $NOT_IGNORED) or (") {
                it.substring(it.indexOf('@'))
            }

        LOGGER.info("Created '$tagstring' from '$value'")

        return tagstring
    }

    fun fetchIngress(applicationName: String): String {
        if (INGRESS_FOR_APPLICATION.isEmpty()) {
            INGRESS_FOR_APPLICATION.putAll(Environment.fetchIngresses())
        }

        return INGRESS_FOR_APPLICATION[applicationName] ?: throw IllegalStateException("Ingen ingress spesifisert for $applicationName!")
    }

    fun clearIngressCache() {
        INGRESS_FOR_APPLICATION.clear()
    }

    override fun toString(): String {
        return "IngressesAndTags($value)"
    }
}
