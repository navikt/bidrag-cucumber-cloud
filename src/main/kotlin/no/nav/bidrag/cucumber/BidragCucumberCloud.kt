package no.nav.bidrag.cucumber

import io.cucumber.core.cli.Main
import no.nav.bidrag.cucumber.model.TagGenerator
import no.nav.bidrag.cucumber.model.TestFailedException
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

        fun run() {
            val tagGenerator = TagGenerator(Environment.ingressesForTags)
            val tags = tagGenerator.hentUtTags()

            val result = Main.run(
                ABSOLUTE_CLOUD_PATH, "--glue", "no.nav.bidrag.cucumber.cloud", "--tags", tags
            )

            if (result != 0.toByte()) {
                val message = "Kjøring av cucumber var mislykket (tags: $tags)!"
                LOGGER.error(message)
                throw TestFailedException(message)
            }
        }
    }
}
