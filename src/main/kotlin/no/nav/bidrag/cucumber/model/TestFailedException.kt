package no.nav.bidrag.cucumber.model

class TestFailedException(message: String, internal val suppressedStackTraceLog: String) : RuntimeException(message)
