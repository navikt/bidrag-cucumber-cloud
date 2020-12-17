package no.nav.bidrag.cucumber.nais.first

import org.assertj.core.api.Assertions.assertThatNullPointerException
import org.junit.jupiter.api.Test

internal class LambdaEgenskaperTest {
    @Test
    fun `will fail when not used by cucumber`() {
        assertThatNullPointerException().isThrownBy { LambdaEgenskaper() }
    }
}