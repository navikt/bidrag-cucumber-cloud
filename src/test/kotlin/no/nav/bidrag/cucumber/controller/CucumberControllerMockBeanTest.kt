package no.nav.bidrag.cucumber.controller

import no.nav.bidrag.cucumber.TestUtil
import no.nav.bidrag.cucumber.model.CucumberTests
import no.nav.bidrag.cucumber.model.TestFailedException
import no.nav.bidrag.cucumber.service.TestService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.io.ByteArrayOutputStream

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("CucumberController (mock bean test)")
internal class CucumberControllerMockBeanTest {

    @Autowired
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private lateinit var testRestTemplate: TestRestTemplate

    @MockBean
    private lateinit var testServiceMock: TestService

    @Test
    @Suppress("NonAsciiCharacters")
    fun `skal ha endpoint som kjører cucumber-tester`() {
        val responseEntity = testRestTemplate.postForEntity("/run", HttpEntity("{}"), Void::class.java)

        assertThat(responseEntity.statusCode).isNotEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `skal angi ingress for tag som skal testes`() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val testResponse = testRestTemplate.postForEntity(
            "/run",
            HttpEntity(
                """
                {
                 |"ingressesForTags":["ingress@tag"]
                }
                """.trimMargin().trim(), headers
            ),
            Void::class.java
        )

        assertThat(testResponse.statusCode).isEqualTo(HttpStatus.OK)

        verify(testServiceMock).run(CucumberTests(listOf("ingress@tag")))
    }

    @Test
    fun `skal angi om det er sanity check`() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val testResponse = testRestTemplate.postForEntity(
            "/run",
            HttpEntity(
                """
                {
                 |"sanityCheck":true
                }
                """.trimMargin().trim(), headers
            ),
            Void::class.java
        )

        assertThat(testResponse.statusCode).isEqualTo(HttpStatus.OK)

        verify(testServiceMock).run(CucumberTests(sanityCheck = true))
    }

    @Test
    fun `skal sende med manuelt generert sikkerhetstoken`() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val testResponse = testRestTemplate.postForEntity(
            "/run",
            HttpEntity(
                """
                {
                 |"securityToken":"xyz..."
                }
                """.trimMargin().trim(), headers
            ),
            Void::class.java
        )

        assertThat(testResponse.statusCode).isEqualTo(HttpStatus.OK)

        verify(testServiceMock).run(CucumberTests(securityToken = "xyz..."))
    }

    @Test
    fun `skal ha HttpStatus 406 når testing med cucumber feiler`() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        whenever(testServiceMock.run(CucumberTests())).thenThrow(TestFailedException("not ok", ByteArrayOutputStream()))

        val testResponse = testRestTemplate.postForEntity(
            "/run", HttpEntity("{}", headers), Void::class.java
        )

        assertAll(
            { assertThat(testResponse.statusCode).`as`("status code").isEqualTo(HttpStatus.NOT_ACCEPTABLE) },
            {
                val warning = testResponse.headers[HttpHeaders.WARNING]?.first() ?: fail("fant ingen feilmelding fra WARNING-header")
                assertThat(warning).`as`("warning").isEqualTo("TestFailedException: not ok")
            }
        )
    }

    @Test
    fun `skal ha HttpStatus 500 ved uventet exception`() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        whenever(testServiceMock.run(CucumberTests())).thenThrow(IllegalStateException("something fishy happened"))

        val testResponse = testRestTemplate.postForEntity(
            "/run", HttpEntity("{}", headers), Void::class.java
        )

        assertAll(
            { assertThat(testResponse.statusCode).`as`("status code").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR) },
            {
                val warning = testResponse.headers[HttpHeaders.WARNING]?.first() ?: fail("fant ingen feilmelding fra WARNING-header")
                assertThat(warning).`as`("warning").isEqualTo("IllegalStateException: something fishy happened")
            }
        )
    }

    @Test
    fun `skal angi testbruker for testing`() {
        TestUtil.assumeThatActuatorHealthIsRunning("https://bidrag-sak.dev.intern.nav.no", "bidrag-sak")
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val testResponse = testRestTemplate.postForEntity(
            "/run",
            HttpEntity(
                """
                {
                 |"ingressesForTags":["https://bidrag-sak.dev.intern.nav.no@bidrag-sak"],
                 |"testUsername":"z993902",
                 |"sanityCheck":true
                }
                """.trimMargin().trim(), headers
            ),
            Void::class.java
        )

        assertAll(
            { assertThat(testResponse.statusCode).isEqualTo(HttpStatus.OK) },
            {
                verify(testServiceMock).run(
                    CucumberTests(
                        sanityCheck = true, testUsername = "z993902", ingressesForTags = listOf("https://bidrag-sak.dev.intern.nav.no@bidrag-sak")
                    )
                )
            }
        )
    }
}
