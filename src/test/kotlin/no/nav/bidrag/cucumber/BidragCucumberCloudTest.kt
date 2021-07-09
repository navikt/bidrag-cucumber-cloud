package no.nav.bidrag.cucumber

import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled("bare for kjøring på naisdevice")
internal class BidragCucumberCloudTest {

    @BeforeEach
    fun `fjern eventuell gammel cache av ingresser`() {
        CacheRestTemplateMedBaseUrl.clearIngressCache()
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `gitt at ingen programargument blir gitt, så skal kjøring av BidragCucumberCloud feile`() {
        assertThatIllegalStateException().isThrownBy { BidragCucumberCloud.main(arrayOf()) }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `gitt SANITY_CHECK=true og ingress@nais-app som programargument, så skal kjøring av BidragCucumberCloud være OK`() {
        System.setProperty(SANITY_CHECK, "true")
        BidragCucumberCloud.main(arrayOf("https://bidrag-sak.dev.intern.nav.no@bidrag-sak"))
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `gitt SANITY_CHECK og to ingress@nais-app som programargument, så skal kjøring av BidragCucumberCloud være OK`() {
        System.setProperty(SANITY_CHECK, "true")
        BidragCucumberCloud.main(arrayOf("https://bidrag-beregn-forskudd-rest.dev.adeo.no@bidrag-beregn-forskudd-rest", "https://bidrag-sak.dev.intern.nav.no@bidrag-sak"))
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `gitt ingress@nais-app som programargument for nais-app uten sikkerhet, så skal kjøring av BidragCucumberCloud være OK`() {
        System.setProperty(SANITY_CHECK, "false")
        BidragCucumberCloud.main(arrayOf("https://bidrag-beregn-forskudd-rest.dev.adeo.no@bidrag-beregn-forskudd-rest"))
    }
}