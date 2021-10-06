package no.nav.bidrag.cucumber.cloud

import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.RestTjeneste
import no.nav.bidrag.cucumber.model.BidragCucumberSingletons
import org.slf4j.LoggerFactory

object FellesEgenskaperService {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(FellesEgenskaperService::class.java)

    @JvmStatic
    private val RESTTJENESTER = ThreadLocal<RestTjeneste>()

    fun assertWhenNotSanityCheck(assertion: Assertion) {
        LOGGER.info(
            "Assertion, actual: '${assertion.value}' - (class: ${assertion.value?.javaClass}), " +
                    "wanted: '${assertion.expectation}' (class: ${assertion.expectation?.javaClass}), " +
                    "sanity check: ${Environment.isSanityCheck}"
        )

        if (Environment.isNotSanityCheck()) {
            assertion.doVerify()
        }
    }

    fun settOppNaisApp(naisApplikasjon: String) {
        LOGGER.info("Setter opp $naisApplikasjon")
        RESTTJENESTER.set(RestTjeneste(naisApplikasjon))
    }

    fun hentRestTjeneste() = RESTTJENESTER.get() ?: throw IllegalStateException("Ingen resttjenester for tråd. Har du satt opp ingressesForApps?")
    fun fjernResttjenester() = RESTTJENESTER.remove()

    data class Assertion(val message: String, val value: Any?, val expectation: Any?, val verify: (input: Assertion) -> Unit) {
        fun doVerify() {
            try {
                verify(this)
            } catch (throwable: Throwable) {
                BidragCucumberSingletons.holdExceptionForTest(throwable)
                throw throwable
            }
        }
    }
}
