package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.BidragCucumberCloud.SANITY_CHECK
import no.nav.bidrag.cucumber.BidragCucumberCloud.TEST_INGRESSES
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.junit.jupiter.api.Test

internal class BidragCucumberCloudTest {

    @Test
    @Suppress("NonAsciiCharacters")
    fun `gitt at miljø ikke er satt opp, så skal kjøring av BidragCucumberCloud feile`() {
        assertThatIllegalStateException().isThrownBy { BidragCucumberCloud.main(arrayOf()) }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `gitt SANITY_CHECK, TEST_INGRESSES og tag som programargument, så skal kjøring av BidragCucumberCloud være OK`() {
        System.setProperty(SANITY_CHECK, "true")
        System.setProperty(TEST_INGRESSES, "bidrag-sak@https://bidrag-sak.dev.intern.nav.no")
        BidragCucumberCloud.main(arrayOf("bidrag-sak"))
    }
}