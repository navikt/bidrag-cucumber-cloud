package no.nav.bidrag.cucumber

import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Environment")
internal class EnvironmentTest {
    companion object {
        private const val TEST_USER_JACTOR_RISES = "TEST_USER_JACTOR-RISES"
    }

    @BeforeEach
    fun `fjern brukernavn og passord`() {
        System.clearProperty(TEST_USER)
        System.clearProperty(TEST_USER_JACTOR_RISES)
    }

    @Test
    fun `skal hente passord basert på testUsername`() {
        System.setProperty(TEST_USER, "jactor-rises")
        assertThatIllegalStateException().isThrownBy { Environment.testUserAuth }
            .withMessageContaining(TEST_USER_JACTOR_RISES)
    }
}
