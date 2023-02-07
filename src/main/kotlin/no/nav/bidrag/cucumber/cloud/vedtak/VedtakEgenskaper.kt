package no.nav.bidrag.cucumber.cloud.vedtak

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.model.CucumberTestRun
import no.nav.bidrag.cucumber.model.CucumberTestRun.Companion.hentRestTjenesteTilTesting
import org.slf4j.LoggerFactory

@Suppress("unused") // used by cucumber
class VedtakEgenskaper : No {
  companion object {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(VedtakEgenskaper::class.java)
  }

  init {

    Så("skal vedtakId lagres") {
      val vedtakId = hentRestTjenesteTilTesting().hentResponse()!!
      LOGGER.info("Opprettet vedtak med vedtakId = $vedtakId")
      CucumberTestRun.thisRun().testData.lagreData("vedtakId" to vedtakId)
    }
  }
}
