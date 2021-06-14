package no.nav.bidrag.cucumber.cloud.beregn

import com.jayway.jsonpath.JsonPath
import io.cucumber.java8.No
import no.nav.bidrag.cucumber.BidragScenario
import org.assertj.core.api.Assertions.assertThat
import org.slf4j.LoggerFactory
import java.io.File

private const val BEREGN_RESOURCES = "src/main/resources/no/nav/bidrag/cucumber/cloud/beregn"
private val LOGGER = LoggerFactory.getLogger(BeregnEgenskaper::class.java)

class BeregnEgenskaper : No {
    companion object {
        private val PROJECT_PATH = File("").absolutePath
    }

    init {
        Når("jeg bruker endpoint {string} med json fra {string}") { endpoint: String, jsonFilePath: String ->
            LOGGER.info("Leser $PROJECT_PATH/$BEREGN_RESOURCES/$jsonFilePath")
            val jsonFile = File("$BEREGN_RESOURCES/$jsonFilePath")
            val json = jsonFile.readText(Charsets.UTF_8)

            BidragScenario.restTjeneste.exchangePost(endpoint, json)
        }

        Og("responsen skal inneholde beløpet {string} under stien {string}") { belop: String, sti: String ->
            val documentContext = JsonPath.parse(BidragScenario.restTjeneste.hentResponse())
            var resultatBelop = documentContext.read<Any>(sti).toString()

            if (resultatBelop.endsWith(".0")) {
                resultatBelop = resultatBelop.removeSuffix(".0")
            }

            assertThat(resultatBelop).isEqualTo(belop)
        }

        Og("responsen skal inneholde resultatkoden {string} under stien {string}") { resultatkode: String, sti: String ->
            val documentContext = JsonPath.parse(BidragScenario.restTjeneste.hentResponse())
            val kode = documentContext.read<Any>(sti).toString()

            assertThat(kode).isEqualTo(resultatkode)
        }
    }
}
