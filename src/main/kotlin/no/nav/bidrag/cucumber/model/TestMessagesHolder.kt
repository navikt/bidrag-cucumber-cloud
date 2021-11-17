package no.nav.bidrag.cucumber.model

internal class TestMessagesHolder {
    private val testMessages: MutableList<String> = ArrayList()

    fun fetchTestMessages() = testMessages.joinToString(separator = "\n")
    fun hold(testMessage: String) = testMessages.add(testMessage)
    fun hold(messages: List<String>) = messages.forEach { hold(it) }
}
