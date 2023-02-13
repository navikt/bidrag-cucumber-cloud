package no.nav.bidrag.cucumber.cloud.stonad

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.model.CucumberTestRun
import org.slf4j.LoggerFactory

@Suppress("unused") // used by cucumber
class StonadEgenskaper : No {
  companion object {
    @JvmStatic
    private val LOGGER = LoggerFactory.getLogger(StonadEgenskaper::class.java)
  }

  init {

    Og("jeg venter i to sekunder slik at vedtakhendelsen kan bli behandlet") {
      LOGGER.info("Vent i to sekunder slik at hendelsen kan bli behandlet")
      if (CucumberTestRun.isNotSanityCheck) Thread.sleep(2000)
    }
  }
}
