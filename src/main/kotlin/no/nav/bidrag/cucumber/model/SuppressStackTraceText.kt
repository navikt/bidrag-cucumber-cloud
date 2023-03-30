package no.nav.bidrag.cucumber.model

class SuppressStackTraceText {
    fun suppress(text: String): String {
        val lines = text
            .split('\n')
            .filter { doNotSuppress(it) }

        return lines.joinToString(separator = "\n")
    }

    internal fun doNotSuppress(line: String): Boolean {
        if (line.startsWith("\tat ")) {
            if (line.contains("no.nav.")) {
                return true
            }

            if (line.contains("/no/nav/")) {
                return true
            }

            return false
        }

        return true
    }
}
