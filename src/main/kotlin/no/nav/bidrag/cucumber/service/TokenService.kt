package no.nav.bidrag.cucumber.service

import no.nav.bidrag.cucumber.dto.SaksbehandlerType
import no.nav.bidrag.cucumber.model.CucumberTestRun

abstract class TokenService {
    abstract fun generateToken(
        application: String,
        saksbehandlerType: SaksbehandlerType? = null,
    ): String

    fun getToken(
        application: String,
        saksbehandlerType: SaksbehandlerType? = null,
    ): String {
        val token =
            if (CucumberTestRun.isNotSanityCheck) {
                generateToken(application, saksbehandlerType)
            } else {
                "sanity check: no token"
            }
        return token
    }
}
