package no.nav.bidrag.cucumber.model

import kotlin.math.log

class TestMessagesHolder {
    companion object {
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
    }

    private fun hentMeldinger(): MutableList<String> {
        if (TEST_MESSAGES_FOR_THREAD.get() == null) {
            TEST_MESSAGES_FOR_THREAD.set(ArrayList())
        }

        return TEST_MESSAGES_FOR_THREAD.get()!!
    }

    fun hold(messages: List<String>) = messages.forEach { hold(it) }
}
