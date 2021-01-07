package no.nav.bidrag.cucumber

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class NaisConfigurationTest {

    @BeforeEach
    fun updateSystemPropetiesForEnvironment() {
        val naisProjectFolder = File("src/test/resources").absolutePath
        System.setProperty(PROJECT_NAIS_FOLDER, naisProjectFolder)
        System.setProperty(ENVIRONMENT, ENVIRONMENT_MAIN)
    }

    @Test
    fun `skal ikke bruke sikkerhet`() {
        val sikkerTeknologi = NaisConfiguration().read("bidrag-beregn-forskudd-rest")

        assertThat(sikkerTeknologi).isEqualTo(SecurityToken.NONE)
    }

    @Test
    fun `skal bruke azure som sikkerhet`() {
        val sikkerTeknologi = NaisConfiguration().read("bidrag-azure-app")

        assertThat(sikkerTeknologi).isEqualTo(SecurityToken.AZURE)
    }
}
