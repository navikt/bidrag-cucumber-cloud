package no.nav.bidrag.cucumber.controller

import no.nav.bidrag.cucumber.CacheRestTemplateMedBaseUrl
import no.nav.bidrag.cucumber.INGRESSES_FOR_TAGS
import no.nav.bidrag.cucumber.SANITY_CHECK
import no.nav.bidrag.cucumber.TestUtil.assumeThatActuatorHealthIsRunning
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("CucumberController (integration test)")
internal class CucumberControllerIntegrationTest {

    @Autowired
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private lateinit var testRestTemplate: TestRestTemplate

    @BeforeEach
    fun `fjern system props`() {
        System.clearProperty(INGRESSES_FOR_TAGS)
        System.clearProperty(SANITY_CHECK)
    }

    @BeforeEach
    fun `tøm caching av ingresser`() {
        CacheRestTemplateMedBaseUrl.clearIngressCache()
    }

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
                 |"ingressesForTags":["https://bidrag-sak.dev.intern.nav.no@bidrag-sak"]
                }
                """.trimMargin().trim(), headers
            ),
            Void::class.java
        )

        assertThat(testResponse.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
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
                 |"ingressesForTags":["https://bidrag-sak.dev.intern.nav.no@bidrag-sak"],
                 |"sanityCheck":true
                }
                """.trimMargin().trim(), headers
            ),
            Void::class.java
        )

        assertThat(testResponse.statusCode).isEqualTo(HttpStatus.OK)
    }
}
