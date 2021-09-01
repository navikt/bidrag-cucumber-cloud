package no.nav.bidrag.cucumber.controller

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.cucumber.BidragCucumberCloudLocal
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@SpringBootTest(classes = [BidragCucumberCloudLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("CucumberController (RestTemplate mock test)")
class CucumberControllerRestTemplateMockBeanTest {

    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @MockBean
    private lateinit var httpHeaderRestTemplateMock: HttpHeaderRestTemplate

    @Test
    fun `skal lage endpoint url mot bidrag-sak`() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val testResponse = testRestTemplate.postForEntity(
            "/run",
            HttpEntity(
                """
                {
                  "testUsername":"z992903","ingressesForApps":[
                    "https://bidrag-sak-feature.dev-fss-pub.nais.io@bidrag-sak"
                  ]
                }
                """.trimMargin().trim(), headers
            ),
            Void::class.java
        )

        val urlCaptor = ArgumentCaptor.forClass(String::class.java)

        verify(httpHeaderRestTemplateMock, atLeastOnce()).exchange(
            urlCaptor.capture(), eq(HttpMethod.GET), any(), eq(String::class.java)
        )

        assertAll(
            { assertThat(testResponse.statusCode).`as`("status code").isEqualTo(HttpStatus.NOT_ACCEPTABLE) },
            { assertThat(urlCaptor.value).`as`("endpoint url").isEqualTo("/sak/1900000") }
        )
    }

    @Test
    fun `skal trekke ut logginnslag til egen bønne som brukes i http resultat`() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val testResponse = testRestTemplate.postForEntity(
            "/run",
            HttpEntity(
                """
                {
                  "testUsername":"z992903","ingressesForApps":[
                    "https://bidrag-sak-feature.dev-fss-pub.nais.io@bidrag-sak"
                  ]
                }
                """.trimMargin().trim(), headers
            ),
            String::class.java
        )

        val testMessages = testResponse.body ?: "Ingen body i response: $testResponse"

        val softly = SoftAssertions()
        softly.assertThat(testMessages).contains("Link")
        softly.assertThat(testMessages).contains("Scenario")
        softly.assertAll()
    }
}
