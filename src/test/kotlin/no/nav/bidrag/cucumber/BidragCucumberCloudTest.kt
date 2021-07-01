package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.BidragCucumberCloud.SANITY_CHECK
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.junit.jupiter.api.Test

internal class BidragCucumberCloudTest {

    @Test
    @Suppress("NonAsciiCharacters")
    fun `gitt at ingen programargument blir gitt, så skal kjøring av BidragCucumberCloud feile`() {
        assertThatIllegalStateException().isThrownBy { BidragCucumberCloud.main(arrayOf()) }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `gitt SANITY_CHECK og ingress@nais-app som programargument, så skal kjøring av BidragCucumberCloud være OK`() {
        System.setProperty(SANITY_CHECK, "true")
        BidragCucumberCloud.main(arrayOf("https://bidrag-sak.dev.intern.nav.no@bidrag-sak"))
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `gitt SANITY_CHECK og to ingress@nais-app som programargument, så skal kjøring av BidragCucumberCloud være OK`() {
        System.setProperty(SANITY_CHECK, "true")
        BidragCucumberCloud.main(arrayOf("https://bidrag-beregn-forskudd-rest.dev.adeo.no@bidrag-beregn-forskudd-rest", "https://bidrag-sak.dev.intern.nav.no@bidrag-sak"))
    }
}