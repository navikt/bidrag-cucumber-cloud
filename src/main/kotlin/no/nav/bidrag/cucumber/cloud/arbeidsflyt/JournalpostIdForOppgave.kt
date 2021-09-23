package no.nav.bidrag.cucumber.cloud.arbeidsflyt

object JournalpostIdForOppgave {

    @JvmStatic
    private val ID_MED_HENDELSE_DTOS = ThreadLocal<AlleJournalpostIdForOppgave>()

    fun fjernIdForHendelser() {
        ID_MED_HENDELSE_DTOS.remove()
    }

    private fun hent(): AlleJournalpostIdForOppgave {
        var dtos = ID_MED_HENDELSE_DTOS.get()

        if (dtos == null) {
            dtos = AlleJournalpostIdForOppgave()
            ID_MED_HENDELSE_DTOS.set(dtos)
        }

        return dtos
    }

    fun leggTil(hendelse: Hendelse, journalpostId: Long, tema: String) = hent().leggTil(JournalpostIdForOppgave(hendelse, journalpostId, tema))
    fun hentJournalpostId(hendelse: Hendelse, tema: String): Long = hentDto(hendelse, tema).journalpostId
    fun hentPrefiksetJournalpostId(hendelse: Hendelse, tema: String): String = hentDto(hendelse, tema).hentPrefiksetId()
    private fun hentDto(hendelse: Hendelse, tema: String): JournalpostIdForOppgave = hent().finnFor(hendelse, tema)

    enum class Hendelse {
        AVVIK_ENDRE_FAGOMRADE
    }

    private data class AlleJournalpostIdForOppgave(private val alle: MutableSet<JournalpostIdForOppgave> = HashSet()) {
        fun leggTil(journalpostIdForOppgave: JournalpostIdForOppgave) = alle.add(journalpostIdForOppgave)
        fun finnFor(hendelse: Hendelse, tema: String): JournalpostIdForOppgave = alle
            .filter { it.hendelse == hendelse }
            .filter { it.tema == tema }
            .stream().findAny()
            .orElseThrow { IllegalArgumentException("Kunne ikke finne instans for hendelse '$hendelse' og tema '$tema' blant $alle") }
    }

    private data class JournalpostIdForOppgave(val hendelse: Hendelse, val journalpostId: Long, val tema: String) {
        fun hentPrefiksetId() = "$tema-$journalpostId"
    }
}
