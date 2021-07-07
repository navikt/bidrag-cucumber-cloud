package no.nav.bidrag.cucumber

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.junit.jupiter.api.Test

internal class TagGeneratorTest {

    @Test
    @Suppress("NonAsciiCharacters")
    fun `gitt at ingen argument til konstruktør blir gitt, så oppstår feil`() {
        assertThatIllegalStateException().isThrownBy { TagGenerator(emptyArray()) }
    }

    @Test
    fun `skal ha property av alle ingresser for tags som en streng`() {
        val tagGenerator = TagGenerator(arrayOf("ingress.en@app.en", "ingress.to@app.to"))
        assertThat(tagGenerator.ingressesForTags).isEqualTo("ingress.en@app.en,ingress.to@app.to")
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal hente ut tag som skal kjøres fra argument`() {
        val tagGenerator = TagGenerator(arrayOf("ingress@app"))
        assertThat(tagGenerator.hentUtTags()).isEqualTo("(@app and not @ignored)")
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal hente ut taggene som skal kjøres fra argumentene`() {
        val tagGenerator = TagGenerator(arrayOf("ingress@app.x", "ingress@app.y"))
        assertThat(tagGenerator.hentUtTags()).isEqualTo("(@app.x and not @ignored) or (@app.y and not @ignored)")
    }

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal hente ut taggene som skal kjøres fra argumentet`() {
        val tagGenerator = TagGenerator(arrayOf("ingress@app.y,ingress@app.z"))
        assertThat(tagGenerator.hentUtTags()).isEqualTo("(@app.y and not @ignored) or (@app.z and not @ignored)")
    }
}