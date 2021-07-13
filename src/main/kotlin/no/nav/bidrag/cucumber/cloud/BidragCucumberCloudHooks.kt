package no.nav.bidrag.cucumber.cloud

import io.cucumber.java8.No
import io.cucumber.java8.Scenario
import no.nav.bidrag.cucumber.ScenarioManager

class BidragCucumberCloudHooks : No {
    init {
        Before(10) { scenario: Scenario ->
            ScenarioManager.use(scenario)
        }

        After(10) { scenario: Scenario ->
            ScenarioManager.reset(scenario)
        }
    }
}