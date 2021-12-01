package no.nav.bidrag.cucumber.service

import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.model.CucumberTestRun

abstract class TokenService {
    abstract fun generateToken(application: String): String

    fun cacheGeneratedToken(application: String): String {
        val token = if (Environment.withSecurityToken) {
            Environment.securityToken!!
        } else if (CucumberTestRun.withSecurityToken) {
            CucumberTestRun.securityToken!!
        } else if (CucumberTestRun.isNotSanityCheck) {
            generateToken(application)
        } else {
            "sanity check: no token"
        }

        CucumberTestRun.updateSecurityToken(token)

        return token
    }
}