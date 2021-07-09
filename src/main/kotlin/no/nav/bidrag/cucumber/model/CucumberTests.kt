package no.nav.bidrag.cucumber.model

data class CucumberTests(
    var ingressesForTags: List<String> = emptyList(),
    var sanityCheck: Boolean = false,
    var testUsername: String? = null
) {
    fun getSanityCheck() = sanityCheck.toString()
    fun hasTestUsername() = testUsername != null
    fun ingressesForTagsAsString(): String {
        val string = ingressesForTags.joinToString(separator = ",")

        if (string.isBlank()) {
            throw IllegalStateException("ingen ingress(er) for tag(s)")
        }

        return string
    }
}
