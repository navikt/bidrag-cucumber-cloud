package no.nav.bidrag.cucumber.cloud

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService.hentRestTjeneste
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService.Assertion
import org.assertj.core.api.Assertions.assertThat
import org.springframework.http.HttpStatus
import java.util.EnumSet

class FellesEgenskaper : No {

    init {
        Gitt("nais applikasjon {string}") { naisApplikasjon: String -> FellesEgenskaperService.settOppNaisApp(naisApplikasjon) }

        Så("skal http status være {int}") { enHttpStatus: Int ->
            FellesEgenskaperService.assertWhenNotSanityCheck(
                Assertion(
                    "HttpStatus for ${hentRestTjeneste().hentEndpointUrl()}",
                    hentRestTjeneste().hentHttpStatus(),
                    HttpStatus.valueOf(enHttpStatus)
                ), this::harForventetHttpStatus
            )
        }

        Og("responsen skal inneholde {string} = {string}") { key: String, value: String ->
            val responseObject = hentRestTjeneste().hentResponseSomMap()
            val verdiFraResponse = responseObject[key]?.toString()

            assertThat(verdiFraResponse).`as`("json response").isEqualTo(value)
        }

        Når("det gjøres et kall til {string}") { endpointUrl: String ->
            hentRestTjeneste().exchangeGet(endpointUrl)
        }

        Så("skal http status ikke være {int} eller {int}") { enHttpStatus: Int, enAnnenHttpStatus: Int ->
            assertThat(hentRestTjeneste().hentHttpStatus())
                .`as`("HttpStatus for " + hentRestTjeneste().hentEndpointUrl())
                .isNotIn(EnumSet.of(HttpStatus.valueOf(enHttpStatus), HttpStatus.valueOf(enAnnenHttpStatus)))
        }
    }

    fun harForventetHttpStatus(assertion: Assertion) {
        assertThat(assertion.value).`as`(assertion.message).isEqualTo(assertion.expectation)
    }
}
