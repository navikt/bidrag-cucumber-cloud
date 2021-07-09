package no.nav.bidrag.cucumber.service

import no.nav.bidrag.cucumber.BidragCucumberCloud
import no.nav.bidrag.cucumber.INGRESSES_FOR_TAGS
import no.nav.bidrag.cucumber.SANITY_CHECK
import no.nav.bidrag.cucumber.TEST_USER
import no.nav.bidrag.cucumber.model.CucumberTests
import org.springframework.stereotype.Service

@Service
class TestService {

    fun run(cucumberTests: CucumberTests) {
        System.setProperty(INGRESSES_FOR_TAGS, cucumberTests.ingressesForTagsAsString())
        System.setProperty(SANITY_CHECK, cucumberTests.getSanityCheck())

        if (cucumberTests.hasTestUsername()) {
            System.setProperty(TEST_USER, cucumberTests.testUsername!!)
        }

        BidragCucumberCloud.run()
    }
}
