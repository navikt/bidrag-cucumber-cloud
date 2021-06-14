package no.nav.bidrag.cucumber.cloud

import io.cucumber.core.cli.Main
import no.nav.bidrag.cucumber.BidragCucumberCloud
import no.nav.bidrag.cucumber.Environment
import org.opentest4j.AssertionFailedError
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger(BidragCucumberCloud::class.java)

fun main(args: Array<String>) {
    val taggedTest = Environment.fetchIntegrationInput().taggedTest
    val tags = if (args.isEmpty() && taggedTest == null) {
        "not @ignored"
    } else if (args.isNotEmpty()) {
        args.joinToString(" or ") { "(@$it and not @ignored)" }
    } else {
        "@$taggedTest and not @ignored"
    }

    LOGGER.info("Running tests for tags: $tags")

    val result = Main.run(
        "-g", "no.nav.bidrag",
        "-p", "pretty",
        "--tags", tags
    )

    if (result.toInt() != 0) {
        throw AssertionFailedError("Forventet resultatet av kjøringen til å være 0, men var $result")
    }
}
