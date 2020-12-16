package no.nav.bidrag.cucumber.nais

import io.cucumber.core.cli.Main
import org.opentest4j.AssertionFailedError

fun main() {
    val result = Main.run(
        "src/main/resources",
        "-g", "no.nav.medlemskap",
        "-p", "pretty",
        "classpath:features",
        "--tags", "not @ignored",
    )

    if (result.toInt() != 0) {
        throw AssertionFailedError("Forventet resultatet av kjøringen til å være 0 men var $result")
    }
}
