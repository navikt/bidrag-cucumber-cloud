package no.nav.bidrag.cucumber.cloud

import org.assertj.core.api.Assertions.assertThat

data class Assertion(
    val message: String,
    val value: Any?,
    val expectation: Any?
) {
    fun check() {
        assertThat(value).`as`(message).isEqualTo(expectation)
    }
}