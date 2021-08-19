package no.nav.bidrag.cucumber.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import no.nav.bidrag.cucumber.model.CucumberTests
import no.nav.bidrag.cucumber.service.CucumberService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/run")
class CucumberController(private val cucumberService: CucumberService) {

    @PostMapping
    @Operation(summary = "Run cucumber tests determined by input")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Angitte tester kjørt uten feil"),
            ApiResponse(responseCode = "406", description = "Testkjøring med cucumber feilet")
        ]
    )
    fun run(@RequestBody cucumberTests: CucumberTests): ResponseEntity<String> {
        return ResponseEntity(cucumberService.run(cucumberTests), HttpStatus.OK)
    }
}