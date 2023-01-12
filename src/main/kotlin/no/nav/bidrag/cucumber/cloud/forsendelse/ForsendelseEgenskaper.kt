package no.nav.bidrag.cucumber.cloud.forsendelse

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import io.cucumber.java8.No
import no.nav.bidrag.cucumber.ABSOLUTE_CLOUD_PATH
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService
import no.nav.bidrag.cucumber.model.Assertion
import no.nav.bidrag.cucumber.model.CucumberTestRun
import no.nav.bidrag.cucumber.model.CucumberTestRun.Companion.hentRestTjenesteTilTesting
import no.nav.bidrag.cucumber.model.CucumberTestRun.Companion.settOppNaisApp
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilNotNull
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Duration

@Suppress("unused") // used by cucumber
class ForsendelseEgenskaper : No {
  companion object {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(ForsendelseEgenskaper::class.java)
  }

  init {

    Og("skal hente opprettet forsendelse") {
      val response = hentRestTjenesteTilTesting().hentResponse()
      val json = parseJson(response)
      val forsendelseId = json!!.get("forsendelseId").asText()
      val dokmentreferanser = json.get("dokumenter").toList().map { it.get("dokumentreferanse").asText() }
      CucumberTestRun.thisRun().testData.lagreDataMedNøkkel("forsendelse", mapOf("forsendelsId" to forsendelseId, "dokmentreferanser" to dokmentreferanser))
      hentRestTjenesteTilTesting().exchangeGet("/api/forsendelse/journal/$forsendelseId")
    }

    Så("skal lagre opprettet forsendelse respons") {
      val response = hentRestTjenesteTilTesting().hentResponse()
      val json = parseJson(response)
      val forsendelseId = json!!.get("forsendelseId").asText()
      val dokmentreferanser = json.get("dokumenter").toList().map { it.get("dokumentreferanse").asText() }
      CucumberTestRun.thisRun().testData.lagreDataMedNøkkel("forsendelse", mapOf("forsendelseId" to forsendelseId, "dokmentreferanser" to dokmentreferanser))
    }

    Og("forsendelse skal inneholde dokument med dokumentmal {string} og status {string}") { malid: String, status: String ->
      val forsendelseId = CucumberTestRun.thisRun().testData.hentDataMedNøkkel("forsendelse")?.get("forsendelseId") as String
      await.pollInSameThread().ignoreExceptions().atMost(Duration.ofSeconds(10)).until {
        val response = hentRestTjenesteTilTesting().exchangeGet("/api/forsendelse/journal/$forsendelseId").body

        val json = parseJson(response)
        val dokumenter = json!!.get("journalpost").get("dokumenter").toList()

        println("hererer")
        val dokument = dokumenter.find { it.get("dokumentmalId").asText() == malid }
        FellesEgenskaperService.assertWhenNotSanityCheck(
          Assertion(
            message = "Dokument",
            value = dokument?.get("status")?.asText(),
            expectation = status
          ) { assertThat(it.value).`as`(it.message).isEqualTo(it.expectation) }
        )

      }

    }

  }

  private fun parseJson(response: String?): JsonNode? {
    if (response == null) {
      return null
    }

    return ObjectMapper().findAndRegisterModules().readTree(response)
  }
}