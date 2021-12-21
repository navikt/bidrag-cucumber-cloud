package no.nav.bidrag.cucumber.logback

import ch.qos.logback.classic.spi.ILoggingEvent
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder
import no.nav.bidrag.cucumber.ScenarioManager
import no.nav.bidrag.cucumber.model.CucumberTestRun

class TestMessageBeforeLayoutHolder : LoggingEventCompositeJsonEncoder() {

    override fun encode(event: ILoggingEvent?): ByteArray {
        if (CucumberTestRun.isTestRunStarted) {
            val message = event?.message ?: throw IllegalStateException("ILoggingEvent should not be null!")
            CucumberTestRun.holdTestMessage(message)
            ScenarioManager.log(message)
        }

        return super.encode(event)
    }
}
