package no.nav.bidrag.cucumber.controller

import no.nav.bidrag.cucumber.model.TestFailedException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException

@RestControllerAdvice
class TestFailedAdvice {

    @ResponseBody
    @ExceptionHandler
    fun handleHttClientErrorException(httpClientErrorException: HttpClientErrorException): ResponseEntity<*> {
        return ResponseEntity
            .status(httpClientErrorException.statusCode)
            .header(HttpHeaders.WARNING, warningFrom(httpClientErrorException))
            .build<Any>()
    }

    @ResponseBody
    @ExceptionHandler
    fun handleTestFailedException(testFailedException: TestFailedException): ResponseEntity<*> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .header(HttpHeaders.WARNING, warningFrom(testFailedException))
            .build<Any>()
    }

    private fun warningFrom(runtimeException: RuntimeException) = "${runtimeException.javaClass.simpleName}: ${runtimeException.message}"
}