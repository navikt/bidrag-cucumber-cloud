package no.nav.bidrag.cucumber.cloud.beregn

import com.jayway.jsonpath.JsonPath
import io.cucumber.java8.No
import no.nav.bidrag.cucumber.ABSOLUTE_CLOUD_PATH
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService
import no.nav.bidrag.cucumber.model.Assertion
import no.nav.bidrag.cucumber.model.CucumberTestRun
import no.nav.bidrag.cucumber.model.CucumberTestRun.Companion.settOppNaisApp
import org.assertj.core.api.Assertions.assertThat
import org.slf4j.LoggerFactory
import java.io.File

@Suppress("unused") // used by cucumber
class BeregnEgenskaper : No {
  companion object {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(BeregnEgenskaper::class.java)

    @JvmStatic
    private val BEREGN_RESOURCES = "$ABSOLUTE_CLOUD_PATH/beregn"
  }

  init {
    Når("jeg bruker endpoint {string} i applikasjon {string} med json fra {string}") { application: String, endpoint: String, jsonFilePath: String ->
      LOGGER.info("Leser $BEREGN_RESOURCES/$jsonFilePath")
      val jsonFile = File("$BEREGN_RESOURCES/$jsonFilePath")
      val json = jsonFile.readText(Charsets.UTF_8)
      settOppNaisApp(application).exchangePost(endpoint, json)
    }

    Og("responsen skal inneholde beløpet {string} under stien {string}") { belop: String, sti: String ->
      val response = settOppNaisApp("bidrag-beregn-saertilskudd-rest").hentResponse()
      var resultatBelop = parseJson(response, sti) ?: "-1"

      if (resultatBelop.endsWith(".0")) {
        resultatBelop = resultatBelop.removeSuffix(".0")
      }

      FellesEgenskaperService.assertWhenNotSanityCheck(
        Assertion(
          message = "Resultatbeløp",
          value = resultatBelop,
          expectation = belop
        ) { assertThat(it.expectation).`as`(it.message).isEqualTo(it.value) }
      )
    }

    Og("responsen skal inneholde resultatkoden {string} under stien {string}")
    { resultatkode: String, sti: String ->
      val response = settOppNaisApp("bidrag-beregn-saertilskudd-rest").hentResponse()
      val kode = parseJson(response, sti) ?: "null"

      FellesEgenskaperService.assertWhenNotSanityCheck(
        Assertion(
          message = "Resultatkode",
          value = resultatkode,
          expectation = kode
        ) { assertThat(it.expectation).`as`(it.message).isEqualTo(it.value) }
      )
    }
  }

  private fun parseJson(response: String?, sti: String): String? {
    if (response == null) {
      return null
    }

    val documentContext = JsonPath.parse(response)
    return documentContext.read<Any>(sti).toString()
  }
}