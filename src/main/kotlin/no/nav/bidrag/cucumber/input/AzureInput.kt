package no.nav.bidrag.cucumber.input

class AzureInput(
    var authorityEndpoint: String = "https://login.microsoftonline.com",
    var clientId: String = "<not set>",
    var clientSecret: String = "<not set>",
    var name: String = "<not set>",
    var tenant: String = "<not set>"
)
