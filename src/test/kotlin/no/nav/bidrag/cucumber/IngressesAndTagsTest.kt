package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.model.IngressesAndTags
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.junit.jupiter.api.Test

internal class IngressesAndTagsTest {

    @Test
    @Suppress("NonAsciiCharacters")
    fun `gitt at en blank streng blir gitt, så oppstår feil`() {
        assertThatIllegalStateException().isThrownBy { IngressesAndTags("") }
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal hente ut tag som skal kjøres fra argument`() {
        val ingressesAndTags = IngressesAndTags("ingress@app")
        assertThat(ingressesAndTags.fetchTags()).isEqualTo("(@app and not @ignored)")
    }
}