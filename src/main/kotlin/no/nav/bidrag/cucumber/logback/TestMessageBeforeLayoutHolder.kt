package no.nav.bidrag.cucumber.logback

import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import no.nav.bidrag.cucumber.ScenarioManager
import no.nav.bidrag.cucumber.model.CucumberTestRun
import java.lang.IllegalStateException

class TestMessageBeforeLayoutHolder : PatternLayout() {

    override fun doLayout(event: ILoggingEvent?): String {
        if (CucumberTestRun.isTestRunStarted) {
            val message = event?.message ?: throw IllegalStateException("event or event.message should not be null!")
            CucumberTestRun.holdTestMessage(message)
            ScenarioManager.log(message)
        }

        return super.doLayout(event)
    }
}
