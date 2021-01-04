package no.nav.bidrag.cucumber.nais

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.BidragScenario
import no.nav.bidrag.cucumber.RestTjeneste
import org.assertj.core.api.Assertions.assertThat
import org.springframework.http.HttpStatus

class FellesEgenskaper : No {
    init {
        Gitt("nais applikasjon {string}") { naisApplikasjon: String -> BidragScenario.restTjeneste = RestTjeneste(naisApplikasjon) }

        Så("skal http status være {int}") { enHttpStatus: Int ->
            assertThat(BidragScenario.restTjeneste.hentHttpStatus())
                .`as`("HttpStatus for ${BidragScenario.restTjeneste.hentEndpointUrl()}")
                .isEqualTo(HttpStatus.valueOf(enHttpStatus))
        }

        Og("responsen skal inneholde {string} = {string}") { key: String, value: String ->
            val responseObject = BidragScenario.restTjeneste.hentResponseSomMap()
            val verdiFraResponse = responseObject[key]?.toString()

            assertThat(verdiFraResponse).`as`("json response").isEqualTo(value)
        }
    }
}
