package no.nav.bidrag.cucumber.aop

import no.nav.bidrag.cucumber.model.TestFailedException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class TestFailedAdvice {

    @ResponseBody
    @ExceptionHandler
    fun handleTestFailedException(testFailedException: TestFailedException) = ResponseEntity
        .status(HttpStatus.NOT_ACCEPTABLE)
        .header(HttpHeaders.WARNING, warningFrom(testFailedException))
        .body(testFailedException.hentSysOutTekst())

    @ResponseBody
    @ExceptionHandler
    fun handleUnknownExceptions(runtimeException: RuntimeException) = ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .header(HttpHeaders.WARNING, warningFrom(runtimeException))
        .build<Any>()

    private fun warningFrom(runtimeException: RuntimeException) = "${runtimeException.javaClass.simpleName}: ${runtimeException.message}"
}