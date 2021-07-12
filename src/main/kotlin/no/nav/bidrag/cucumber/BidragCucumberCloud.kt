package no.nav.bidrag.cucumber

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
}
