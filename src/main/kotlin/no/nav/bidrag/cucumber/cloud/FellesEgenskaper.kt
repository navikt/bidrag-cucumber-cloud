package no.nav.bidrag.cucumber.cloud

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.BidragScenario
import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.RestTjeneste
import org.assertj.core.api.Assertions.assertThat
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import java.util.EnumSet

class FellesEgenskaper : No {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(FellesEgenskaper::class.java)

        private class Assertion(val message: String, val value: Any, val expectation: Any) {
            fun check() {
                assertThat(value).`as`(message).isEqualTo(expectation)
            }
        }

        private fun sanityCheck(assertion: Assertion) {
            if (Environment.isSanityCheck()) {
                LOGGER.info("No assertion: ${assertion.message}: ${assertion.value} vs ${assertion.expectation}")
            } else {
                assertion.check()
            }
        }
    }

    init {
        Gitt("nais applikasjon {string}") { naisApplikasjon: String -> BidragScenario.restTjeneste = RestTjeneste(naisApplikasjon) }

        Så("skal http status være {int}") { enHttpStatus: Int ->
            sanityCheck(
                Assertion(
                    "HttpStatus for ${BidragScenario.restTjeneste.hentEndpointUrl()}",
                    BidragScenario.restTjeneste.hentHttpStatus(),
                    HttpStatus.valueOf(enHttpStatus)
                )
            )
        }

        Og("responsen skal inneholde {string} = {string}") { key: String, value: String ->
            val responseObject = BidragScenario.restTjeneste.hentResponseSomMap()
            val verdiFraResponse = responseObject[key]?.toString()

            assertThat(verdiFraResponse).`as`("json response").isEqualTo(value)
        }

        Når("det gjøres et kall til {string}") { endpointUrl: String ->
            BidragScenario.restTjeneste.exchangeGet(endpointUrl)
        }

        Så("skal http status ikke være {int} eller {int}") { enHttpStatus: Int, enAnnenHttpStatus: Int ->
            assertThat(BidragScenario.restTjeneste.hentHttpStatus())
                .`as`("HttpStatus for " + BidragScenario.restTjeneste.hentEndpointUrl())
                .isNotIn(EnumSet.of(HttpStatus.valueOf(enHttpStatus), HttpStatus.valueOf(enAnnenHttpStatus)))
        }

        Når("det mangler sikkerhetstoken i HttpRequest") {
            BidragScenario.restTjeneste.removeHeaderGenerator(HttpHeaders.AUTHORIZATION)
        }
    }
}
