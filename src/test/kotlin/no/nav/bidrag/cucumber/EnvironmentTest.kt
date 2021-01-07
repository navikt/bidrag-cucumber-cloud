package no.nav.bidrag.cucumber

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EnvironmentTest {

    @Test
    fun `skal hente ENVIRONMENT_MAIN`() {
        System.setProperty(ENVIRONMENT, ENVIRONMENT_MAIN)
        val miljo = Environment.miljo

        assertThat(miljo).isEqualTo(ENVIRONMENT_MAIN)
    }
}