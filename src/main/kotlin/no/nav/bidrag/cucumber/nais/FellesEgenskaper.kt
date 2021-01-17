package no.nav.bidrag.cucumber.nais

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.BidragScenario
import no.nav.bidrag.cucumber.RestTjeneste
import org.assertj.core.api.Assertions.assertThat
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import java.util.EnumSet

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
