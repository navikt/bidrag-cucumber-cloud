package no.nav.bidrag.cucumber.controller

import no.nav.bidrag.cucumber.BidragCucumberCloudLocal
import no.nav.bidrag.cucumber.TestUtil.assumeThatActuatorHealthIsRunning
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.AssumptionViolatedException
import org.junit.jupiter.api.Disabled
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
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [BidragCucumberCloudLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("CucumberController (integration test)")
@ActiveProfiles("test")
internal class CucumberControllerIntegrationTest {

    @Autowired
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private lateinit var testRestTemplate: TestRestTemplate

    private val ingressIsUp: MutableMap<String, Boolean> = HashMap()

    private fun assumeThatActuatorHealthIsRunningCachedException(ingress: String, app: String) {
        if (!ingressIsUp.contains(ingress)) {
            try {
                assumeThatActuatorHealthIsRunning(ingress, app)
            } catch (ave: AssumptionViolatedException) {
                ingressIsUp[ingress] = false
                throw ave
            } finally {
                ingressIsUp.computeIfAbsent(ingress) { true }
            }
        } else if (ingressIsUp[ingress] == false) {
            throw AssumptionViolatedException("$ingress is not UP")
        }
    }

    @Test
    fun `skal feile ved testing av applikasjon med azure ad`() {
        assumeThatActuatorHealthIsRunningCachedException("https://bidrag-grunnlag.dev.intern.nav.no", "bidrag-grunnlag")

        val testResponse = testRestTemplate.postForEntity(
            "/run",
            HttpEntity(
                """
                {
                  "ingressesForApps":["https://bidrag-sak.dev.intern.nav.no@tag:bidrag-grunnlag"]
                }
                """.trimMargin().trim(), initJsonAsMediaType()
            ),
            Void::class.java
        )

        assertThat(testResponse.statusCode).isEqualTo(HttpStatus.NOT_ACCEPTABLE)
    }

    @Test
    @Disabled
    fun `skal ikke feile ved testing av applikasjon med azure ad når det er snakk om en sanity check`() {
        assumeThatActuatorHealthIsRunningCachedException("https://bidrag-grunnlag.dev.intern.nav.no", "bidrag-grunnlag")

        val testResponse = testRestTemplate.postForEntity(
            "/run",
            HttpEntity(
                """
                {
                  "ingressesForApps":["https://bidrag-grunnlag.dev.intern.nav.no@tag:bidrag-grunnlag"],
                  "sanityCheck":true
                }
                """.trimMargin().trim(), initJsonAsMediaType()
            ),
            Void::class.java
        )

        assertThat(testResponse.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `skal hente ut cucumber tekst fra kjøring`() {
        assumeThatActuatorHealthIsRunningCachedException("https://bidrag-cucumber-cloud-feature.ekstern.dev.nav.no", "bidrag-cucumber-cloud")

        val testResponse = testRestTemplate.postForEntity(
            "/run",
            HttpEntity(
                """
                {
                  "ingressesForApps":["https://bidrag-cucumber-cloud.ekstern.dev.nav.no@tag:bidrag-cucumber-cloud"],
                  "sanityCheck":true
                }
                """.trimMargin().trim(), initJsonAsMediaType()
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
    @Disabled
    fun `skal ikke feile når det er sanity check`() {
        assumeThatActuatorHealthIsRunningCachedException("https://bidrag-grunnlag.dev.intern.nav.no", "bidrag-grunnlag")

        val testResponse = testRestTemplate.postForEntity(
            "/run",
            HttpEntity(
                """
                {
                  "ingressesForApps":["https://bidrag-sak.dev.intern.nav.no@tag:bidrag-grunnlag"],
                  "sanityCheck":true
                }
                """.trimMargin().trim(), initJsonAsMediaType()
            ),
            String::class.java
        )

        assertThat(testResponse.statusCode).`as`("status code").isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `skal logge eventuelle exception når det feiler under testing`() {
        val testResponse = testRestTemplate.postForEntity(
            "/run",
            HttpEntity(
                """
                {
                  "ingressesForApps":["https://bidrag-sak.dev.intern.nav.no@bidrag-sak"],
                  "testUsername":"ukjent","tags":["@bidrag-cucumber-cloud"]
                }
                """.trimMargin().trim(), initJsonAsMediaType()
            ),
            String::class.java
        )

        assertAll(
            { assertThat(testResponse.statusCode).`as`("status code").isEqualTo(HttpStatus.NOT_ACCEPTABLE) },
            { assertThat(testResponse.body).`as`("body").contains("Failure details:") }
        )
    }

    private fun initJsonAsMediaType(): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        return headers
    }
}
