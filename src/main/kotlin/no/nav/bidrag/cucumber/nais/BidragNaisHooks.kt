package no.nav.bidrag.cucumber.nais

import io.cucumber.java8.No
import io.cucumber.java8.Scenario
import no.nav.bidrag.cucumber.BidragCucumberCloud

class BidragNaisHooks : No {
    init {
        Before(10) { scenario: Scenario ->
            BidragCucumberCloud.use(scenario)
        }

        After(10) {  scenario: Scenario ->
            BidragCucumberCloud.reset(scenario)
        }
    }
}