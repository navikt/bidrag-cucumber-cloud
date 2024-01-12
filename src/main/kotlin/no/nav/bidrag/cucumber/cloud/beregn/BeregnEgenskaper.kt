package no.nav.bidrag.cucumber.cloud.beregn

import com.jayway.jsonpath.JsonPath
import io.cucumber.java8.No
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService
import no.nav.bidrag.cucumber.model.Assertion
import no.nav.bidrag.cucumber.model.CucumberTestRun.Companion.hentRestTjenesteTilTesting
import org.assertj.core.api.Assertions.assertThat
import org.slf4j.LoggerFactory

@Suppress("unused") // used by cucumber
class BeregnEgenskaper : No {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(BeregnEgenskaper::class.java)
    }

    init {

        Og("responsen skal inneholde beløpet {string} under stien {string}") { belop: String, sti: String ->
            val response = hentRestTjenesteTilTesting().hentResponse()
            var resultatBelop = parseJson(response, sti) ?: "-1"

            if (resultatBelop.endsWith(".0")) {
                resultatBelop = resultatBelop.removeSuffix(".0")
            }

            FellesEgenskaperService.assertWhenNotSanityCheck(
                Assertion(
                    message = "Resultatbeløp",
                    value = resultatBelop,
                    expectation = belop,
                ) { assertThat(it.expectation).`as`(it.message).isEqualTo(it.value) },
            )
        }

        Og("responsen skal inneholde resultatkoden {string} under stien {string}") { resultatkode: String, sti: String ->
            val response = hentRestTjenesteTilTesting().hentResponse()
            val kode = parseJson(response, sti) ?: "null"

            FellesEgenskaperService.assertWhenNotSanityCheck(
                Assertion(
                    message = "Resultatkode",
                    value = resultatkode,
                    expectation = kode,
                ) { assertThat(it.expectation).`as`(it.message).isEqualTo(it.value) },
            )
        }
    }

    private fun parseJson(
        response: String?,
        sti: String,
    ): String? {
        if (response == null) {
            return null
        }

        val documentContext = JsonPath.parse(response)
        return documentContext.read<Any>(sti).toString()
    }
}
