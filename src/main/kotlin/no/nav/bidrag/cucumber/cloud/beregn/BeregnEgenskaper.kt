package no.nav.bidrag.cucumber.cloud.beregn

import com.jayway.jsonpath.JsonPath
import io.cucumber.java8.No
import no.nav.bidrag.cucumber.ABSOLUTE_CLOUD_PATH
import no.nav.bidrag.cucumber.BidragScenario
import org.assertj.core.api.Assertions.assertThat
import org.slf4j.LoggerFactory
import java.io.File

class BeregnEgenskaper : No {
    companion object {
        private val BEREGN_RESOURCES = "$ABSOLUTE_CLOUD_PATH/beregn"
        private val LOGGER = LoggerFactory.getLogger(BeregnEgenskaper::class.java)
    }

    init {
        Når("jeg bruker endpoint {string} med json fra {string}") { endpoint: String, jsonFilePath: String ->
            LOGGER.info("Leser $BEREGN_RESOURCES/$jsonFilePath")
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
