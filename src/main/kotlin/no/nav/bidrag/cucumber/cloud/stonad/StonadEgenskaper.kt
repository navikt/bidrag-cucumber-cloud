package no.nav.bidrag.cucumber.cloud.stonad

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.model.CucumberTestRun
import no.nav.bidrag.cucumber.model.CucumberTestRun.Companion.hentRestTjenesteTilTesting
import no.nav.bidrag.cucumber.model.parseJson
import org.assertj.core.api.Assertions
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime

@Suppress("unused") // used by cucumber
class StonadEgenskaper : No {
  companion object {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(StonadEgenskaper::class.java)
  }

  init {

    Når("jeg venter i {long} millisekunder slik at vedtakhendelsen kan bli behandlet") { millisekunder: Long ->
      LOGGER.info("Vent i $millisekunder millisekunder slik at hendelsen kan bli behandlet")
      if (CucumberTestRun.isNotSanityCheck) Thread.sleep(millisekunder)
    }

    Og("responsen under stien {string} skal være maks {long} sekunder gammel") { sti: String, antallSekunder: Long ->
      LOGGER.info("Sjekker at vedtak-hendelsen har oppdatert endretTidspunkt på stønaden og at denne er maks $antallSekunder sekunder gammel")
      val response = hentRestTjenesteTilTesting().hentResponse()
      val endretTidspunkt = LocalDateTime.parse(parseJson(response, sti)) ?: LocalDateTime.MIN
      val naaTidspunkt = LocalDateTime.now();
      val differanse = Duration.between(endretTidspunkt, naaTidspunkt)

      if (CucumberTestRun.isNotSanityCheck) {
        Assertions.assertThat(differanse.seconds).isLessThan(antallSekunder)
      }
    }
  }
}
