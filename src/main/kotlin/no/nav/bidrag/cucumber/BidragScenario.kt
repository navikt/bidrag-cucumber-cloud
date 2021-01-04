package no.nav.bidrag.cucumber

/**
 * Instanser som er gyldige i et scenario og er felles for flere cucumber steg/egenskaper
 */
class BidragScenario {
    companion object {
        internal lateinit var restTjeneste: RestTjeneste
    }
}