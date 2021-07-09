package no.nav.bidrag.cucumber.model

data class CucumberTests(
    var ingressesForTags: List<String> = emptyList(),
    var sanityCheck: Boolean = false
) {
    fun getSanityCheck() = sanityCheck.toString()
    fun ingressesForTagsAsString(): String {
        val string = ingressesForTags.joinToString(separator = ",")

        if (string.isBlank()) {
            throw IllegalStateException("ingen ingress(er) for tag(s)")
        }

        return string
    }
}
