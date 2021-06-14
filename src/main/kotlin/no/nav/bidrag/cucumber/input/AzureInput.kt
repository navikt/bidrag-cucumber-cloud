package no.nav.bidrag.cucumber.input

import no.nav.bidrag.cucumber.Environment

class AzureInput(
    var authorityEndpoint: String = Environment.AZURE_LOGIN_ENDPOINT,
    var clientId: String = "<not set>",
    var clientSecret: String = "<not set>",
    var name: String = "<not set>",
    var tenant: String = "<not set>"
)
