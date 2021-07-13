package no.nav.bidrag.cucumber.model

import java.lang.RuntimeException

class TestFailedException(message: String) : RuntimeException(message)