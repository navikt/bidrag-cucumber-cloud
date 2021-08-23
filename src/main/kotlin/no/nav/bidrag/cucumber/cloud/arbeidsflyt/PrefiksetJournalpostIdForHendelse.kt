package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import java.util.EnumMap

class PrefiksetJournalpostIdForHendelse {
    internal val PREFIKSET_ID_FOR_HENDELER = ThreadLocal<MutableMap<Hendelse, MutableMap<String, String>>>()

    fun opprett(hendelse: Hendelse, journalpostId: Long, tema: String) {
        val prefiksetIdForHendelse: MutableMap<Hendelse, MutableMap<String, String>> = EnumMap(Hendelse::class.java)
        val prefiksetIdForTema: MutableMap<String, String> = HashMap()
        prefiksetIdForTema[tema] = "$tema-$journalpostId"
        prefiksetIdForHendelse[hendelse] = prefiksetIdForTema
        PREFIKSET_ID_FOR_HENDELER.set(prefiksetIdForHendelse)
    }

    fun hent(hendelse: Hendelse, tema: String) = PREFIKSET_ID_FOR_HENDELER.get()[hendelse]?.get(tema) ?: throw IllegalStateException(
        "Ingen journalpostId konfigurert for $hendelse på tema $tema: ${PREFIKSET_ID_FOR_HENDELER.get()}"
    )

    enum class Hendelse {
        AVVIK_ENDRE_FAGOMRADE
    }
}
