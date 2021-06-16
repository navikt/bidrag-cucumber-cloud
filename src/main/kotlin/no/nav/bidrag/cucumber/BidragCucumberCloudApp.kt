package no.nav.bidrag.cucumber

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class BidragCucumberCloudApp {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val app = SpringApplication(BidragCucumberCloudApp::class.java)
            app.run(*args)
        }
    }
}
