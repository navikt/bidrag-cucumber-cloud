package no.nav.bidrag.cucumber.controller

import no.nav.bidrag.cucumber.model.CucumberTests
import no.nav.bidrag.cucumber.service.TestService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/run")
class CucumberController(private val testService: TestService) {

    @PostMapping
    fun run(@RequestBody cucumberTests: CucumberTests) {
         testService.run(cucumberTests)
    }
}