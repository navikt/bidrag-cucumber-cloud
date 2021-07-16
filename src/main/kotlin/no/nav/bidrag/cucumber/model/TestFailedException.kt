package no.nav.bidrag.cucumber.model

import java.io.ByteArrayOutputStream
import java.lang.RuntimeException
import java.nio.charset.Charset

class TestFailedException(message: String, private val sysOut: ByteArrayOutputStream) : RuntimeException(message) {
    fun hentSysOutTekst() = sysOut.toString(Charset.defaultCharset())
}