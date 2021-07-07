package no.nav.bidrag.cucumber

import org.slf4j.LoggerFactory

class TagGenerator(ingressesForTags: Array<String>) {
    companion object {
        const val NOT_IGNORED = "not @ignored"
        private val LOGGER = LoggerFactory.getLogger(TagGenerator::class.java)
    }

    internal val ingressesForTags = ingressesForTags.joinToString(separator = ",")

    init {
        if (ingressesForTags.isEmpty()) {
            val message = "Ingen ingress(er) med tag som argument!"
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
