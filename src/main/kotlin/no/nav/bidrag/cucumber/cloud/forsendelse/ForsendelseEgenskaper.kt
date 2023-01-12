package no.nav.bidrag.cucumber.cloud.forsendelse

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.cucumber.java8.No
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService
import no.nav.bidrag.cucumber.model.Assertion
import no.nav.bidrag.cucumber.model.CucumberTestRun
import no.nav.bidrag.cucumber.model.CucumberTestRun.Companion.hentRestTjenesteTilTesting
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.slf4j.LoggerFactory
import java.time.Duration

val FORSENDELSE_NØKKEL = "forsendelse"

@Suppress("unused") // used by cucumber
class ForsendelseEgenskaper : No {
  companion object {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(ForsendelseEgenskaper::class.java)
  }

  init {

    Så("skal lagre opprettet forsendelse detaljer") {
      val response = hentRestTjenesteTilTesting().hentResponse()
      val json = parseJson(response)
      val forsendelseId = json!!.get("forsendelseId").asText()
      CucumberTestRun.thisRun().testData.lagreData("forsendelseId" to forsendelseId)
    }

    Så("skal lagre opprettet journalpost detaljer") {
      val response = hentRestTjenesteTilTesting().hentResponse()
      val json = parseJson(response)
      val journalpostId = json!!.get("journalpostId").asText()
      val dokumenter = json.get("dokumenter").toList().map { it.get("dokumentreferanse").asText() }
      CucumberTestRun.thisRun().testData.lagreData("journalpostId" to journalpostId)
      CucumberTestRun.thisRun().testData.lagreData("joark_dokumentreferanse" to dokumenter.get(0))
    }

    Og("forsendelse inneholder dokument med dokumentmal {string} og status {string}") { malid: String, status: String ->
      val forsendelseId = CucumberTestRun.thisRun().testData.hentDataMedNøkkel("forsendelseId") as String
      await.pollInSameThread().pollInterval(Duration.ofMillis(500)).ignoreExceptions().atMost(Duration.ofSeconds(5)).until {
        val response = hentRestTjenesteTilTesting().exchangeGet("/api/forsendelse/journal/$forsendelseId").body

        val json = parseJson(response)
        val dokumenter = json!!.get("journalpost").get("dokumenter").toList()

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

    Og("forsendelse inneholder {int} dokumenter") { antall: Int ->
      val forsendelseId = CucumberTestRun.thisRun().testData.hentDataMedNøkkel("forsendelseId")
      val response = hentRestTjenesteTilTesting().exchangeGet("/api/forsendelse/journal/$forsendelseId").body

      val json = parseJson(response)
      val dokumenter = json!!.get("journalpost").get("dokumenter").toList()

      FellesEgenskaperService.assertWhenNotSanityCheck(
        Assertion(
          message = "Dokument",
          value = dokumenter.size,
          expectation = antall
        ) { assertThat(it.value).`as`(it.message).isEqualTo(it.expectation) }
      )
    }
    Og("forsendelse inneholder joark journalpostid") {
      val forsendelseId = CucumberTestRun.thisRun().testData.hentDataMedNøkkel("forsendelseId")
      val response = hentRestTjenesteTilTesting().exchangeGet("/api/forsendelse/journal/$forsendelseId").body

      val json = parseJson(response)
      val journalpostId = json!!.get("journalpost").get("joarkJournalpostId").asText()

      FellesEgenskaperService.assertWhenNotSanityCheck(
        Assertion(
          message = "Dokument",
          value = journalpostId,
          expectation = journalpostId
        ) { assertThat(it.value).`as`(it.message).isNotNull() }
      )
    }
  }


  private fun parseJson(response: String?): JsonNode? {
    if (response == null) {
      return null
    }

    return ObjectMapper().findAndRegisterModules().readTree(response)
  }
}