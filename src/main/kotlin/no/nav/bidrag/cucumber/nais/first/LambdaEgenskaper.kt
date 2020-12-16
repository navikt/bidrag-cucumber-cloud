package no.nav.bidrag.cucumber.nais.first

import io.cucumber.java8.No
import org.assertj.core.api.Assertions.assertThat

class LambdaEgenskaper : No {
    private lateinit var navn: String

    init {
        Gitt("at første steg kjøres av {string}") { navn: String ->
            this.navn = navn
        }

        Så("skal det andre steget si {string}") { sier: String ->
            assertThat(sier).isEqualTo("Hei $navn!")
        }
    }
}