package no.nav.bidrag.cucumber.cloud

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.model.CucumberTestRun
import org.slf4j.LoggerFactory

@Suppress("unused") // brukes av cucumber
class TestDataEgenskaper : No {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(FellesEgenskaper::class.java)
    }
    init {
        Og("nøkkel for testdata {string}") { nokkel: String ->
            CucumberTestRun.thisRun().testData.initialiserData(nokkel)
        }
    }
}
