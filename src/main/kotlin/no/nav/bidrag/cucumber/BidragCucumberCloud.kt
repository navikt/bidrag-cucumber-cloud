package no.nav.bidrag.cucumber

import io.cucumber.core.cli.Main
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BidragCucumberCloud {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(BidragCucumberCloud::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<BidragCucumberCloud>(*args)
        }
    }

    fun run(args: Array<String>) {
        val tagGenerator = TagGenerator(Environment.ingressesForTags)
        val tags = tagGenerator.hentUtTags()
        System.setProperty(INGRESSES_FOR_TAGS, tagGenerator.ingressesForTags)

        val result = Main.run(
            ABSOLUTE_CLOUD_PATH, "--glue", "no.nav.bidrag.cucumber.cloud", "--tags", tags
        )

        if (result != 0.toByte()) {
            val message = "Kjøring av cucumber var mislykket (tags: $tags)!"
            LOGGER.error(message)
            throw IllegalStateException(message)
        }
    }
}
