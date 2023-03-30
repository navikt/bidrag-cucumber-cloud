package no.nav.bidrag.cucumber.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("RunStats")
internal class RunStatsTest {

    private val runStats = RunStats()

    @Test
    fun `skal lage feilmeldinger med riktige innrykk`() {
        val feilmeldinger = listOf(
            "en feil oppstod:",
            """
            det var noe skit...
            det skal ikke skje igjen
            """.trimIndent()
        )

        runStats.addExceptionLogging(feilmeldinger)

        val nyeFeilmeldinger = listOf(
            """
            svarte...
            det skjedde igjen...
            """.trimIndent(),
            "men nå er det slutt!"
        )
        runStats.addExceptionLogging(nyeFeilmeldinger)
        val failureDetails = runStats.createStringOfFailureDetails().trim()

        assertThat(failureDetails).isEqualTo(
            """
            Failure details:
            - en feil oppstod:
              det var noe skit...
              det skal ikke skje igjen
            - svarte...
              det skjedde igjen...
              men nå er det slutt!
            """.trimIndent().trim()
        )
    }
}
