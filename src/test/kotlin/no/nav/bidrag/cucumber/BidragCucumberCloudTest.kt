package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.TestUtil.assumeThatActuatorHealthIsRunning
import no.nav.bidrag.cucumber.service.TestService
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BidragCucumberCloudTest {

    @BeforeEach
    fun `fjern eventuell gammel cache av ingresser`() {
        RestTjenesteForApplikasjon.clearIngressCache()
    }
}