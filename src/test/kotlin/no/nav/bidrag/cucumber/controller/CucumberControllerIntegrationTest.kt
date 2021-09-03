package no.nav.bidrag.cucumber.controller

import no.nav.bidrag.cucumber.BidragCucumberCloudLocal
import no.nav.bidrag.cucumber.TEST_AUTH
import no.nav.bidrag.cucumber.TestUtil.assumeThatActuatorHealthIsRunning
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@SpringBootTest(classes = [BidragCucumberCloudLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("CucumberController (integration test)")
internal class CucumberControllerIntegrationTest {

    @Autowired
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private lateinit var testRestTemplate: TestRestTemplate

    @Test
    fun `skal feile ved testing av applikasjon med azure ad`() {
        assumeThatActuatorHealthIsRunning("https://bidrag-sak.dev.intern.nav.no", "bidrag-sak")
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val testResponse = testRestTemplate.postForEntity(
            "/run",
            HttpEntity(
                """
                {
                  "ingressesForApps":["https://bidrag-sak.dev.intern.nav.no@bidrag-sak"]
                }
                """.trimMargin().trim(), headers
            ),
            Void::class.java
        )

        assertThat(testResponse.statusCode).isEqualTo(HttpStatus.NOT_ACCEPTABLE)
    }

    @Test
    fun `skal ikke feile ved testing av applikasjon med azure ad når det er snakk om en sanity check`() {
        assumeThatActuatorHealthIsRunning("https://bidrag-sak.dev.intern.nav.no", "bidrag-sak")
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val testResponse = testRestTemplate.postForEntity(
            "/run",
            HttpEntity(
                """
                {
                  "ingressesForApps":["https://bidrag-sak.dev.intern.nav.no@bidrag-sak"],
                  "sanityCheck":true
                }
                """.trimMargin().trim(), headers
            ),
            Void::class.java
        )

        assertThat(testResponse.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `skal hente ut cucumber tekst fra kjøring`() {
        assumeThatActuatorHealthIsRunning("https://bidrag-sak.dev.intern.nav.no", "bidrag-sak")
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val testResponse = testRestTemplate.postForEntity(
            "/run",
            HttpEntity(
                """
                {
                  "ingressesForApps":["https://bidrag-sak.dev.intern.nav.no@bidrag-sak"],
                  "sanityCheck":true
                }
                """.trimMargin().trim(), headers
            ),
            String::class.java
        )

        val softly = SoftAssertions()
        softly.assertThat(testResponse.body).`as`("body").contains("Scenarios")
        softly.assertThat(testResponse.body).`as`("body").contains("Failed")
        softly.assertThat(testResponse.body).`as`("body").contains("Passed")
        softly.assertAll()
    }

    @Test
    fun `skal ikke feile når det er sanity check selv om det sendes med brukernavn til en testbruker`() {
        assumeThatActuatorHealthIsRunning("https://bidrag-sak.dev.intern.nav.no", "bidrag-sak")
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val testResponse = testRestTemplate.postForEntity(
            "/run",
            HttpEntity(
                """
                {
                  "ingressesForApps":["https://bidrag-sak.dev.intern.nav.no@bidrag-sak"],
                  "sanityCheck":true,
                  "testUsername":"z992903"
                }
                """.trimMargin().trim(), headers
            ),
            String::class.java
        )

        assertThat(testResponse.statusCode).`as`("status code").isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `skal logge eventuelle exception når det feiler under testing`() {
        assumeThatActuatorHealthIsRunning("https://bidrag-sak.dev.intern.nav.no", "bidrag-sak")
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val testResponse = testRestTemplate.postForEntity(
            "/run",
            HttpEntity(
                """
                {
                  "ingressesForApps":["https://bidrag-sak.dev.intern.nav.no@bidrag-sak"]
                }
                """.trimMargin().trim(), headers
            ),
            String::class.java
        )

        assertAll(
            { assertThat(testResponse.statusCode).`as`("status code").isEqualTo(HttpStatus.NOT_ACCEPTABLE) },
            { assertThat(testResponse.body).`as`("body").containsIgnoringCase("Failure details!") }
        )
    }
}
