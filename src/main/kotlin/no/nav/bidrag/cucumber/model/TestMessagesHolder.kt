package no.nav.bidrag.cucumber.model

import org.slf4j.LoggerFactory

class TestMessagesHolder {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(TestMessagesHolder::class.java)

        @JvmStatic
        private val TEST_MESSAGES_FOR_THREAD = ThreadLocal<MutableList<String>?>()
    }

    fun fetchTestMessages(): String {
        val joinToString = hentMeldinger().joinToString(separator = "\n")
        TEST_MESSAGES_FOR_THREAD.remove()

        return joinToString
    }

    internal fun hold(testMessage: String) {
        hentMeldinger().add(testMessage)
        LOGGER.info(testMessage)
    }

    private fun hentMeldinger(): MutableList<String> {
        if (TEST_MESSAGES_FOR_THREAD.get() == null) {
            TEST_MESSAGES_FOR_THREAD.set(ArrayList())
        }

        return TEST_MESSAGES_FOR_THREAD.get()!!
    }
}
