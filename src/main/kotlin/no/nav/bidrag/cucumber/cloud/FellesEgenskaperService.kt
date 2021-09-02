package no.nav.bidrag.cucumber.cloud

import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.RestTjeneste
import no.nav.bidrag.cucumber.ScenarioManager
import no.nav.bidrag.cucumber.model.BidragCucumberSingletons

object FellesEgenskaperService {
    @JvmStatic
    private val RESTTJENESTER = ThreadLocal<RestTjeneste>()

    fun assertWhenNotSanityCheck(assertion: Assertion) {
        if (Environment.isSanityCheck) {
            ScenarioManager.log("Sanity check - ${assertion.message}: actual - '${assertion.value}', wanted - '${assertion.expectation}'")
        } else {
            assertion.doVerify()
        }
    }

    fun settOppNaisApp(naisApplikasjon: String) {
        ScenarioManager.log("Setter opp $naisApplikasjon")
        RESTTJENESTER.set(RestTjeneste(naisApplikasjon))
    }

    fun hentRestTjeneste() = RESTTJENESTER.get() ?: throw IllegalStateException("Ingen resttjeneste for tråd. Har du satt opp ingressesForApps?")
    fun fjernResttjenester() = RESTTJENESTER.remove()

    data class Assertion(val message: String, val value: Any?, val expectation: Any?, val verify: (input: Assertion) -> Unit) {
        fun doVerify() {
            try {
                verify(this)
            } catch (exception: Exception) {
                BidragCucumberSingletons.holdExceptionForTest(exception)
                throw exception
            }
        }
    }
}
