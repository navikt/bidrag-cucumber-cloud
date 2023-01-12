package no.nav.bidrag.cucumber.cloud

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import io.cucumber.java8.No
import no.nav.bidrag.cucumber.ABSOLUTE_CLOUD_PATH
import no.nav.bidrag.cucumber.cloud.beregn.BeregnEgenskaper
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService
import no.nav.bidrag.cucumber.model.Assertion
import no.nav.bidrag.cucumber.model.CucumberTestRun
import no.nav.bidrag.cucumber.model.CucumberTestRun.Companion.hentRestTjenesteTilTesting
import org.assertj.core.api.Assertions.assertThat
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.io.File
import java.util.EnumSet

@Suppress("unused") // brukes av cucumber
class FellesEgenskaper : No {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(FellesEgenskaper::class.java)
    }
    init {
        Gitt("nais applikasjon {string}") { naisApplikasjon: String -> CucumberTestRun.settOppNaisAppTilTesting(naisApplikasjon) }
        Og("responsen skal inneholde verdien {string} under stien {string}") { forventetVerdi: String, sti: String ->
            val response = hentRestTjenesteTilTesting().hentResponse()
            val json = parseJson(response)

            val stiSplittet = sti.split(".")
//            val verdi = json?.

            FellesEgenskaperService.assertWhenNotSanityCheck(
                Assertion(
                    message = "$sti fra respons skal inneholde verdi $forventetVerdi",
                    value = "resultatBelop",
                    expectation = forventetVerdi
                ) { assertThat(it.expectation).`as`(it.message).isEqualTo(it.value) }
            )
        }

        Når("jeg bruker endpoint {string} med json fra {string}") { endpoint: String, jsonFilePath: String ->
            LOGGER.info("Leser $ABSOLUTE_CLOUD_PATH/$jsonFilePath")
            val jsonFile = File("$ABSOLUTE_CLOUD_PATH/$jsonFilePath")
            val json = jsonFile.readText(Charsets.UTF_8)
            hentRestTjenesteTilTesting().exchangePost(endpoint, json)
        }

        Så("skal http status være {int}") { enHttpStatus: Int ->
            FellesEgenskaperService.assertWhenNotSanityCheck(
                Assertion(
                    "HttpStatus for ${hentRestTjenesteTilTesting().hentFullUrlMedEventuellWarning()}",
                    hentRestTjenesteTilTesting().hentHttpStatus(),
                    HttpStatus.valueOf(enHttpStatus)
                ) { assertThat(it.value).`as`(it.message).isEqualTo(it.expectation) }
            )
        }

        Og("responsen skal inneholde {string} = {string}") { key: String, value: String ->
            val responseObject = hentRestTjenesteTilTesting().hentResponseSomMap()
            val verdiFraResponse = responseObject[key]?.toString()

            assertThat(verdiFraResponse).`as`("json response").isEqualTo(value)
        }

        Når("det gjøres et kall til {string}") { endpointUrl: String ->
            hentRestTjenesteTilTesting().exchangeGet(endpointUrl)
        }

        Så("skal http status ikke være {int} eller {int}") { enHttpStatus: Int, enAnnenHttpStatus: Int ->
            assertThat(hentRestTjenesteTilTesting().hentHttpStatus())
                .`as`("HttpStatus for " + hentRestTjenesteTilTesting().hentFullUrlMedEventuellWarning())
                .isNotIn(EnumSet.of(HttpStatus.valueOf(enHttpStatus), HttpStatus.valueOf(enAnnenHttpStatus)))
        }
    }

    private fun parseJson(response: String?): JsonNode? {
        if (response == null) {
            return null
        }

        return ObjectMapper().findAndRegisterModules().readTree(response)
    }
}
