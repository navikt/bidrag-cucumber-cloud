package no.nav.bidrag.cucumber.service

import no.nav.bidrag.cucumber.BidragCucumberCloud
import no.nav.bidrag.cucumber.CacheRestTemplateMedBaseUrl
import no.nav.bidrag.cucumber.INGRESSES_FOR_TAGS
import no.nav.bidrag.cucumber.SANITY_CHECK
import no.nav.bidrag.cucumber.SECURITY_TOKEN
import no.nav.bidrag.cucumber.TEST_USER
import no.nav.bidrag.cucumber.model.CucumberTests
import org.springframework.stereotype.Service

@Service
class TestService {

    fun run(cucumberTests: CucumberTests) {
        clearIngressCache()
        clearOldSystemProperties()
        setNewSystemProperties(cucumberTests)

        BidragCucumberCloud.run()
    }

    private fun clearIngressCache() {
        CacheRestTemplateMedBaseUrl.clearIngressCache()
    }

    private fun clearOldSystemProperties() {
        System.clearProperty(INGRESSES_FOR_TAGS)
        System.clearProperty(SANITY_CHECK)
        System.clearProperty(SECURITY_TOKEN)
        System.clearProperty(TEST_USER)
    }

    private fun setNewSystemProperties(cucumberTests: CucumberTests) {
        System.setProperty(INGRESSES_FOR_TAGS, cucumberTests.ingressesForTagsAsString())
        System.setProperty(SANITY_CHECK, cucumberTests.getSanityCheck())

        if (cucumberTests.securityToken != null) {
            System.setProperty(SECURITY_TOKEN, cucumberTests.securityToken!!)
        }

        if (cucumberTests.hasTestUsername()) {
            System.setProperty(TEST_USER, cucumberTests.testUsername!!)
        }
    }
}
