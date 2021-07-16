package no.nav.bidrag.cucumber

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class SystemOutPocTest {

    @Test
    fun `skal lese print-meldinger fra system out`() {
        val sysOut = ByteArrayOutputStream()
        System.setOut(PrintStream(sysOut))
        print("svadalada")

        assertThat(sysOut.toString()).isEqualTo("svadalada")
    }
}