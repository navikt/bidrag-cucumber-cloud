package no.nav.bidrag.cucumber.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import no.nav.bidrag.cucumber.dto.CucumberTestsApi
import no.nav.bidrag.cucumber.model.CucumberTestRun
import no.nav.bidrag.cucumber.service.CucumberService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/run")
class CucumberController(private val cucumberService: CucumberService) {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(CucumberController::class.java)
    }

    @PostMapping
    @Operation(summary = "Run cucumber tests determined by input")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Angitte tester kjørt uten feil"),
            ApiResponse(responseCode = "406", description = "Testkjøring med cucumber feilet")
        ]
    )
    fun run(@RequestBody cucumberTestsApi: CucumberTestsApi): ResponseEntity<String> {
        LOGGER.info("Running cucumber tests with $cucumberTestsApi!")
        val cucumberTestRun = CucumberTestRun(cucumberTestsApi).initEnvironment()
        return ResponseEntity(cucumberService.run(cucumberTestRun), HttpStatus.OK)
    }
}