package no.nav.bidrag.cucumber

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.context.annotation.FilterType

@SpringBootApplication
@ComponentScan(excludeFilters = [Filter(pattern = ["no.nav.bidrag.commons..*", "no.nav.security..*"], type = FilterType.ASPECTJ)])
class BidragCucumberCloud {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(BidragCucumberCloud::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            val profile = if (args.isEmpty()) {
                PROFILE_LIVE
            } else {
                LOGGER.info("Starter med profil (argument): $args")
                args[0]
            }

            val app = SpringApplication(BidragCucumberCloud::class.java)

            app.setAdditionalProfiles(profile)
            app.run(*args)
        }
    }
}
