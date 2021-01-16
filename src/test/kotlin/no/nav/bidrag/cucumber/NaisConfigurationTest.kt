package no.nav.bidrag.cucumber

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class NaisConfigurationTest {

    @Test
    fun `skal ikke bruke sikkerhet`() {
        val sikkerTeknologi = NaisConfiguration().read("bidrag-beregn-forskudd-rest")

        assertThat(sikkerTeknologi).isEqualTo(Security.NONE)
    }

    @Test
    fun `skal bruke azure som sikkerhet`() {
        val sikkerTeknologi = NaisConfiguration().read("bidrag-azure-app")

        assertThat(sikkerTeknologi).isEqualTo(Security.AZURE)
    }
}
