package no.nav.bidrag.cucumber.model


internal class TestData {
    val dataForNokkel: MutableMap<String, Data> = HashMap()

    fun hentDataMedNøkkel(nokkel: String): Data? = dataForNokkel[nokkel]
    fun lagreDataMedNøkkel(nokkel: String, data: Data) {
        dataForNokkel[nokkel] = data
    }

}

typealias Data = Map<String, Any>
