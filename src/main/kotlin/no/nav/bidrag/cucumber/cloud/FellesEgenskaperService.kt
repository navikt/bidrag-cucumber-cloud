package no.nav.bidrag.cucumber.cloud

import no.nav.bidrag.cucumber.model.Assertion
import no.nav.bidrag.cucumber.model.CucumberTestRun
import org.slf4j.LoggerFactory

object FellesEgenskaperService {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(FellesEgenskaperService::class.java)

    fun assertWhenNotSanityCheck(assertion: Assertion): Boolean {
        LOGGER.info(
            "Assertion, actual: '${assertion.value}' - (${assertion.value?.javaClass}), " +
                "wanted: '${assertion.expectation}' (${assertion.expectation?.javaClass}), " +
                "sanity check: ${CucumberTestRun.isSanityCheck}",
        )

        if (CucumberTestRun.isNotSanityCheck) {
            assertion.doVerify()
        }
        return true
    }
}
