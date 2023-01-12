package no.nav.bidrag

import no.nav.bidrag.cucumber.BidragCucumberCloud
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class BidragCucumberCloudLokalNais {
    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val app = SpringApplication(BidragCucumberCloudLokalNais::class.java)

            app.setAdditionalProfiles("lokal-nais-secrets")
            app.run(*args)
        }
    }
}
