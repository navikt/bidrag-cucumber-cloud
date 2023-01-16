package no.nav.bidrag.cucumber.controller

import no.nav.bidrag.cucumber.BidragCucumberCloudLocal
import no.nav.bidrag.cucumber.model.CucumberTestRun
import no.nav.bidrag.cucumber.model.CucumberTestsModel
import no.nav.bidrag.cucumber.model.TestFailedException
import no.nav.bidrag.cucumber.service.CucumberService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.kotlin.any
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
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [BidragCucumberCloudLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("CucumberController (mocked bean: CucumberService)")
@ActiveProfiles("test")
internal class CucumberControllerCucumberServiceMockBeanTest {

    @Autowired
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private lateinit var testRestTemplate: TestRestTemplate

    @MockBean
    private lateinit var cucumberServiceMock: CucumberService

    @Test
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
                  "ingressesForApps":["ingress@tag"]
                }
                """.trimMargin().trim(), headers
            ),
            Void::class.java
        )

        assertThat(testResponse.statusCode).isEqualTo(HttpStatus.OK)

        verify(cucumberServiceMock).run(CucumberTestRun(CucumberTestsModel(ingressesForApps = listOf("ingress@tag"))))
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
                  "sanityCheck":true
                }
                """.trimMargin().trim(), headers
            ),
            Void::class.java
        )

        assertThat(testResponse.statusCode).isEqualTo(HttpStatus.OK)

        verify(cucumberServiceMock).run(CucumberTestRun(CucumberTestsModel(sanityCheck = true)))
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
                  "securityToken":"xyz..."
                }
                """.trimMargin().trim(), headers
            ),
            Void::class.java
        )

        assertThat(testResponse.statusCode).isEqualTo(HttpStatus.OK)

        verify(cucumberServiceMock).run(CucumberTestRun(CucumberTestsModel(securityToken = "xyz...")))
    }

    @Test
    fun `skal ha HttpStatus 406 når testing med cucumber feiler`() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        whenever(cucumberServiceMock.run(any())).thenThrow(TestFailedException("not ok", "test failed"))

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

        whenever(cucumberServiceMock.run(any())).thenThrow(IllegalStateException("something fishy happened"))

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
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val testResponse = testRestTemplate.postForEntity(
            "/run",
            HttpEntity(
                """
                {
                  "ingressesForApps":["https://bidrag-sak.dev.intern.nav.no@bidrag-sak"],
                  "testUsername":"z993902",
                  "sanityCheck":true
                }
                """.trimMargin().trim(), headers
            ),
            Void::class.java
        )

        assertAll(
            { assertThat(testResponse.statusCode).isEqualTo(HttpStatus.OK) },
            {
                verify(cucumberServiceMock).run(
                    CucumberTestRun(
                        CucumberTestsModel(
                            sanityCheck = true, testUsername = "z993902", ingressesForApps = listOf("https://bidrag-sak.dev.intern.nav.no@bidrag-sak")
                        )
                    )
                )
            }
        )
    }

    @Test
    fun `skal sende enkle tags i tillegg til ingress`() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val testResponse = testRestTemplate.postForEntity(
            "/run",
            HttpEntity(
                """
                {
                  "ingressesForApps":["https://some-ingress@some-app"],
                  "tags":["@some-tag"]
                }
                """.trimMargin().trim(), headers
            ), Void::class.java
        )

        assertAll(
            { assertThat(testResponse.statusCode).isEqualTo(HttpStatus.OK) },
            {
                verify(cucumberServiceMock).run(
                    CucumberTestRun(
                        CucumberTestsModel(
                            ingressesForApps = listOf("https://some-ingress@some-app"), tags = listOf("@some-tag")
                        )
                    )
                )
            }
        )
    }
}
