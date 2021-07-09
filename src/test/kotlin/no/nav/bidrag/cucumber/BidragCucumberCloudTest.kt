package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.TestUtil.assumeThatActuatorHealthIsRunning
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BidragCucumberCloudTest {

    @BeforeEach
    fun `fjern eventuell gammel cache av ingresser`() {
        CacheRestTemplateMedBaseUrl.clearIngressCache()
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `gitt at ingen ingresser for tags er satt, så skal kjøring av BidragCucumberCloud feile`() {
        System.clearProperty(INGRESSES_FOR_TAGS)
        assertThatIllegalStateException().isThrownBy { BidragCucumberCloud.run() }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `gitt SANITY_CHECK=true og INGRESSES_FOR_APPS=ingress@nais-app, så skal kjøring av BidragCucumberCloud ikke feile`() {
        assumeThatActuatorHealthIsRunning("https://bidrag-sak.dev.intern.nav.no", "bidrag-sak")
        System.setProperty(SANITY_CHECK, "true")
        System.setProperty(INGRESSES_FOR_TAGS, "https://bidrag-sak.dev.intern.nav.no@bidrag-sak")
        BidragCucumberCloud.run()
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `gitt SANITY_CHECK=false, men INGRESSES_FOR_APPS er for nais-app uten sikkerhet, så skal kjøring av BidragCucumberCloud ikke feile`() {
        assumeThatActuatorHealthIsRunning("https://bidrag-beregn-forskudd-rest.dev.adeo.no", "bidrag-beregn-forskudd-rest")
        System.clearProperty(TEST_USER)
        System.setProperty(SANITY_CHECK, "false")
        System.setProperty(INGRESSES_FOR_TAGS, "https://bidrag-beregn-forskudd-rest.dev.adeo.no@bidrag-beregn-forskudd-rest")
        BidragCucumberCloud.run()
    }
}