package no.nav.bidrag.cucumber.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SuppressStackTraceTextTest {

    private val suppressStackTraceText = SuppressStackTraceText()

    @Test
    fun `skal fjerne ukjent stacktrace`() {
        val text = """com.fasterxml.jackson.core.JsonParseException:
${"\t"}at com.fasterxml.jackson.core.JsonParser._constructError(JsonParser.java:2337)
${"\t"}at com.fasterxml.jackson.core.base.ParserMinimalBase._reportError(ParserMinimalBase.java:720)
${"\t"}at com.fasterxml.jackson.core.json.ReaderBasedJsonParser._reportInvalidToken(ReaderBasedJsonParser.java:2903)
${"\t"}at com.fasterxml.jackson.core.json.ReaderBasedJsonParser._reportInvalidToken(ReaderBasedJsonParser.java:2881)
${"\t"}at com.fasterxml.jackson.core.json.ReaderBasedJsonParser._matchToken(ReaderBasedJsonParser.java:2662)
${"\t"}at com.fasterxml.jackson.core.json.ReaderBasedJsonParser._handleOddValue(ReaderBasedJsonParser.java:1926)
${"\t"}at com.fasterxml.jackson.core.json.ReaderBasedJsonParser.nextToken(ReaderBasedJsonParser.java:781)
${"\t"}at com.fasterxml.jackson.databind.ObjectMapper._initForReading(ObjectMapper.java:4684)
${"\t"}at com.fasterxml.jackson.databind.ObjectMapper._readMapAndClose(ObjectMapper.java:4586)
${"\t"}at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3548)
${"\t"}at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:3516)
${"\t"}at no.nav.bidrag.cucumber.RestTjeneste.hentResponseSomMap(RestTjeneste.kt:32)
${"\t"}at no.nav.bidrag.cucumber.cloud.FellesEgenskaper._init_(FellesEgenskaper.kt:41)"""

        val stackedText = suppressStackTraceText.suppress(text)

        assertThat(stackedText).isEqualTo(
            """com.fasterxml.jackson.core.JsonParseException:
${"\t"}at no.nav.bidrag.cucumber.RestTjeneste.hentResponseSomMap(RestTjeneste.kt:32)
${"\t"}at no.nav.bidrag.cucumber.cloud.FellesEgenskaper._init_(FellesEgenskaper.kt:41)"""
        )
    }

    @Test
    fun `will not suppress line`() {
        assertThat(suppressStackTraceText.doNotSuppress("en text")).isTrue
    }

    @Test
    fun `will suppress line which is a stack trace of some class`() {
        assertThat(suppressStackTraceText.doNotSuppress("\tat some.class")).isFalse
    }

    @Test
    fun `will not suppress stack trace line which of a nav class`() {
        assertThat(suppressStackTraceText.doNotSuppress("\tat no.nav")).isTrue
    }

    @Test
    fun `will not suppress stack trace lines from feature files`() {
        assertThat(suppressStackTraceText.doNotSuppress("\tat some file.feature:")).isTrue
    }
}