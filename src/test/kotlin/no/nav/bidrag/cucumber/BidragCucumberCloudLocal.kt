package no.nav.bidrag.cucumber

import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class BidragCucumberCloudLocal {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            BidragCucumberCloud.main(args)
        }
    }
}
