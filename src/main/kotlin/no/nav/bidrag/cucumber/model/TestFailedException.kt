package no.nav.bidrag.cucumber.model

class TestFailedException(message: String, internal val sysOutText: String) : RuntimeException(message)