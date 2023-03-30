package no.nav.bidrag.cucumber.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import no.nav.bidrag.cucumber.dto.HendelseApi
import no.nav.bidrag.cucumber.hendelse.HendelseProducer
import no.nav.bidrag.cucumber.model.JournalpostHendelse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class HendelseController(private val hendelseProducer: HendelseProducer) {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(HendelseController::class.java)
    }

    @PostMapping("/hendelse/opprett")
    @Operation(summary = "Opprett en journalpost-hendelse")
    @ApiResponse(responseCode = "200", description = "ny journalpost-hendelse er publisert")
    fun opprett(@RequestBody hendelseApi: HendelseApi): ResponseEntity<Void> {
        LOGGER.info("publiserer $hendelseApi")
        hendelseProducer.publish(JournalpostHendelse(hendelseApi))
        return ResponseEntity.ok().build()
    }
}
