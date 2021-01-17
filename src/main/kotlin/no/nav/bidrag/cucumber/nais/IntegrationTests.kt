package no.nav.bidrag.cucumber.nais

import io.cucumber.core.cli.Main
import no.nav.bidrag.cucumber.Environment
import org.opentest4j.AssertionFailedError

fun main(args: Array<String>) {
    val taggedTest = Environment.fetchIntegrationInput().taggedTest
    val tags = if (args.isEmpty() && taggedTest == null) "not @ignored" else if (args.isNotEmpty()) {
        args.joinToString(" or ") { "(@$it and not @ignored)" }
    } else {
        "@$taggedTest and not @ignored"
    }

    val result = Main.run(
        "-g", "no.nav.bidrag",
        "-p", "pretty",
        "--tags", tags
    )

    if (result.toInt() != 0) {
        throw AssertionFailedError("Forventet resultatet av kjøringen til å være 0, men var $result")
    }
}
