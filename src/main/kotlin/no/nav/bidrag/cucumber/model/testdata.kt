package no.nav.bidrag.cucumber.model


internal class TestData {
    val dataForNokkel: MutableMap<String, Data> = HashMap()
    var nokkel: String? = null

    fun hentData(nokkel: String? = null) = dataForNokkel[nokkel ?: this.nokkel] ?: throw IllegalArgumentException("Fant ingen data for nøkkel")
    fun hentDataMedNøkkel(dataNøkkel: String? = null) = hentData()[dataNøkkel]
    fun lagreData(data: Pair<String, String>) {
        hentData()?.put(data.first, data.second)
    }
    fun initialiserData(nokkel: String){
        if (!dataForNokkel.contains(nokkel)){
            this.nokkel = nokkel
            dataForNokkel[nokkel] = HashMap()
        }

    }

}


typealias Data = MutableMap<String, Any>