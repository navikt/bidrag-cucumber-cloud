package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.model.TagGenerator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.junit.jupiter.api.Test

internal class TagGeneratorTest {

    @Test
    @Suppress("NonAsciiCharacters")
    fun `gitt at en blank streng blir gitt til konstruktøren, så oppstår feil`() {
        assertThatIllegalStateException().isThrownBy { TagGenerator("") }
    }

    @Test
    fun `skal ha property av alle ingresser for tags som en streng`() {
        val tagGenerator = TagGenerator("ingress.en@app.en,ingress.to@app.to")
        assertThat(tagGenerator.ingressesForTags).isEqualTo("ingress.en@app.en,ingress.to@app.to")
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal hente ut tag som skal kjøres fra argument`() {
        val tagGenerator = TagGenerator("ingress@app")
        assertThat(tagGenerator.hentUtTags()).isEqualTo("(@app and not @ignored)")
    }
}