package no.nav.bidrag.cucumber.aop

import no.nav.bidrag.commons.ExceptionLogger
import no.nav.bidrag.cucumber.BidragCucumberCloudLocal
import no.nav.bidrag.cucumber.controller.CucumberController
import no.nav.bidrag.cucumber.service.CucumberService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

@DisplayName("ExceptionLoggerAspect")
@SpringBootTest(classes = [BidragCucumberCloudLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ExceptionLoggerAspectTest {

    @Autowired
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private lateinit var testRestTemplate: TestRestTemplate

    @MockBean
    private lateinit var exceptionLoggerMock: ExceptionLogger

    @MockBean
    private lateinit var cucumberServiceMock: CucumberService

    @Test
    fun `skal logge eventuelle exception når feil oppstår i controller`() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val illegalStateException = IllegalStateException("something fishy happened")
        whenever(cucumberServiceMock.run(any())).thenThrow(illegalStateException)

        testRestTemplate.postForEntity("/run", HttpEntity("{}", headers), Void::class.java)

        verify(exceptionLoggerMock).logException(illegalStateException, "class ${CucumberController::class.java.name}")
    }
}