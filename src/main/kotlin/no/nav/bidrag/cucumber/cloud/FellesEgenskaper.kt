package no.nav.bidrag.cucumber.cloud

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.ABSOLUTE_CLOUD_PATH
import no.nav.bidrag.cucumber.model.Assertion
import no.nav.bidrag.cucumber.model.CucumberTestRun
import no.nav.bidrag.cucumber.model.CucumberTestRun.Companion.hentRestTjenesteTilTesting
import org.assertj.core.api.Assertions.assertThat
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.io.File
import java.util.*

@Suppress("unused") // brukes av cucumber
class FellesEgenskaper : No {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(FellesEgenskaper::class.java)
    }
    init {
        Gitt("nais applikasjon {string}") { naisApplikasjon: String -> CucumberTestRun.settOppNaisAppTilTesting(naisApplikasjon) }

        Når("jeg bruker endpoint {string} med json fra {string}") { endpoint: String, jsonFilePath: String ->
            LOGGER.info("Leser $ABSOLUTE_CLOUD_PATH/$jsonFilePath")
            val jsonFile = File("$ABSOLUTE_CLOUD_PATH/$jsonFilePath")
            val json = jsonFile.readText(Charsets.UTF_8)
            hentRestTjenesteTilTesting().exchangePost(TestdataManager.erstattUrlMedParametereFraTestdata(endpoint), json)
        }

        Når("jeg bruker endpoint {string} med json:") { endpoint: String, json: String ->
            hentRestTjenesteTilTesting().exchangePost(TestdataManager.erstattUrlMedParametereFraTestdata(endpoint), TestdataManager.erstattJsonMedParametereFraTestdata(json))
        }

        Når("jeg kaller endepunkt {string}") { endpoint: String ->
            hentRestTjenesteTilTesting().exchangePost(TestdataManager.erstattUrlMedParametereFraTestdata(endpoint))
        }

        Når("jeg kaller endepunkt {string} med http metode {string}") { endpoint: String, metode: String ->
            val endepunktMedParametere = TestdataManager.erstattUrlMedParametereFraTestdata(endpoint)
            when(metode.uppercase()){
                "DELETE"-> hentRestTjenesteTilTesting().exchangeDelete(endepunktMedParametere, "{}")
                "POST"-> hentRestTjenesteTilTesting().exchangePost(endepunktMedParametere, "{}")
                "PATCH"-> hentRestTjenesteTilTesting().exchangePatch(endepunktMedParametere, "{}")
            }

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

        Når("det gjøres et kall til {string} med testdataparameter") { endpointUrl: String ->
            hentRestTjenesteTilTesting().exchangeGet(TestdataManager.erstattUrlMedParametereFraTestdata(endpointUrl))
        }

        Så("skal http status ikke være {int} eller {int}") { enHttpStatus: Int, enAnnenHttpStatus: Int ->
            assertThat(hentRestTjenesteTilTesting().hentHttpStatus())
                .`as`("HttpStatus for " + hentRestTjenesteTilTesting().hentFullUrlMedEventuellWarning())
                .isNotIn(EnumSet.of(HttpStatus.valueOf(enHttpStatus), HttpStatus.valueOf(enAnnenHttpStatus)))
        }
    }
}
