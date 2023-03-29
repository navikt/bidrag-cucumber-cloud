package no.nav.bidrag.cucumber.cloud.grunnlag

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.model.CucumberTestRun
import no.nav.bidrag.cucumber.model.CucumberTestRun.Companion.hentRestTjenesteTilTesting
import org.slf4j.LoggerFactory

@Suppress("unused") // used by cucumber
class GrunnlagEgenskaper : No {
  companion object {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(GrunnlagEgenskaper::class.java)
  }

  init {

    Så("skal grunnlagspakkeId lagres") {
      val grunnlagspakkeId = hentRestTjenesteTilTesting().hentResponse()!!
      LOGGER.info("Opprettet grunnlagspakke med id = $grunnlagspakkeId")
      CucumberTestRun.thisRun().testData.lagreData("grunnlagspakkeId" to grunnlagspakkeId)
    }
  }
}
