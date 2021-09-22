package no.nav.bidrag.cucumber.logback

import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import no.nav.bidrag.cucumber.model.BidragCucumberSingletons
import no.nav.bidrag.cucumber.model.TestMessagesHolder
import java.lang.IllegalStateException

class TestMessageBeforeLayoutHolder : PatternLayout() {
    companion object {
        @JvmStatic
        private val TEST_RUN_STARTED = ThreadLocal<Boolean?>()

        fun startTestRun() {
            TEST_RUN_STARTED.set(true)
        }

        fun endTestRun() {
            TEST_RUN_STARTED.remove()
            TestMessagesHolder.fjernTestMessages()
        }
    }

    override fun doLayout(event: ILoggingEvent?): String {
        if (TEST_RUN_STARTED.get() == true) {
            val message = event?.message ?: throw IllegalStateException("event or event.message should not be null!")
            BidragCucumberSingletons.holdTestMessage(message)
        }

        return super.doLayout(event)
    }
}