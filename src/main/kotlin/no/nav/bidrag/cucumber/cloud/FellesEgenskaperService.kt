package no.nav.bidrag.cucumber.cloud

import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.RestTjeneste
import org.slf4j.LoggerFactory

object FellesEgenskaperService {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(FellesEgenskaper::class.java)

    @JvmStatic
    private val RESTTJENESTER = ThreadLocal<RestTjeneste>()

    fun assertWhenNotSanityCheck(assertion: Assertion, verify: (input: Assertion) -> Unit) {
        if (Environment.isSanityCheck) {
            LOGGER.info("Sanity check - ${assertion.message}: actual - '${assertion.value}', wanted - '${assertion.expectation}'")
        } else {
            verify(assertion)
        }
    }

    fun settOppNaisApp(naisApplikasjon: String) {
        LOGGER.info("Setter opp $naisApplikasjon")
        RESTTJENESTER.set(RestTjeneste(naisApplikasjon))
    }

    fun hentRestTjeneste() = RESTTJENESTER.get() ?: throw IllegalStateException("Ingen resttjeneste for tråd. Har du satt opp ingressesForApps?")
    fun fjernResttjenester() = RESTTJENESTER.remove()

    data class Assertion(val message: String, val value: Any?, val expectation: Any?)
}
