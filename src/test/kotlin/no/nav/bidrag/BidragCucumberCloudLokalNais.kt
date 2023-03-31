package no.nav.bidrag

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType

@SpringBootApplication
@ComponentScan(excludeFilters = [ComponentScan.Filter(pattern = ["no.nav.bidrag.commons..*", "no.nav.security..*"], type = FilterType.ASPECTJ)])
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
