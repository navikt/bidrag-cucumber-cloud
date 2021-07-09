package no.nav.bidrag.cucumber.model

import org.slf4j.LoggerFactory

data class TagGenerator(internal val ingressesForTags: String) {
    companion object {
        const val NOT_IGNORED = "not @ignored"
        private val LOGGER = LoggerFactory.getLogger(TagGenerator::class.java)
    }

    init {
        if (ingressesForTags.isBlank()) {
            val message = "Ingen ingress(er) med tag(s)!"
            LOGGER.error(message)
            throw IllegalStateException(message)
        }
    }

    fun hentUtTags(): String {
        val tagstring = ingressesForTags.split(',')
            .joinToString(prefix = "(", postfix = " and $NOT_IGNORED)", separator = " and $NOT_IGNORED) or (") {
                it.substring(it.indexOf('@'))
            }

        LOGGER.info("Created '$tagstring' from '$ingressesForTags'")

        return tagstring
    }
}
