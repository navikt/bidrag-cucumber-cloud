package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import java.util.EnumMap

class PrefiksetJournalpostIdForHendelse {
    companion object {
        @JvmStatic
        private val PREFIKSET_ID_FOR_HENDELER = ThreadLocal<MutableMap<Hendelse, MutableMap<String, String>>>()

        fun fjernIdForHendelser() {
            PREFIKSET_ID_FOR_HENDELER.remove()
        }
    }

    fun opprett(hendelse: Hendelse, journalpostId: Long, tema: String): String {
        val prefiksetIdForHendelse: MutableMap<Hendelse, MutableMap<String, String>> = EnumMap(Hendelse::class.java)
        val prefiksetIdForTema: MutableMap<String, String> = HashMap()
        val prefiksetId = "$tema-$journalpostId"
        prefiksetIdForTema[tema] = prefiksetId
        prefiksetIdForHendelse[hendelse] = prefiksetIdForTema
        PREFIKSET_ID_FOR_HENDELER.set(prefiksetIdForHendelse)

        return prefiksetId
    }

    fun hent(hendelse: Hendelse, tema: String) = PREFIKSET_ID_FOR_HENDELER.get()[hendelse]?.get(tema) ?: throw IllegalStateException(
        "Ingen journalpostId konfigurert for $hendelse på tema $tema: ${PREFIKSET_ID_FOR_HENDELER.get()}"
    )

    enum class Hendelse {
        AVVIK_ENDRE_FAGOMRADE
    }
}
