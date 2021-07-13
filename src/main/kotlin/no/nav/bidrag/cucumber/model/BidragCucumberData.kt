package no.nav.bidrag.cucumber.model

import no.nav.bidrag.cucumber.RestTjeneste

/**
 * Data som er gyldige i en cucumber-kjøring og som er felles for flere cucumber steg/egenskaper
 */
internal object BidragCucumberData {
    lateinit var restTjeneste: RestTjeneste
}
