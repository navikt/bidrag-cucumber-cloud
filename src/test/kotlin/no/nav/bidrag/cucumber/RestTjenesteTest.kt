package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.model.CucumberTestsDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

internal class RestTjenesteTest {

    @Test
    fun `gitt INGRESSES_FOR_APPS med verdi for applikasjon, skal RestTjeneste konfigureres med denne verdien`() {
        val cucumberTestsDto = CucumberTestsDto(
            ingressesForApps = listOf("https://somewhere.com/@nais-app", "https://somewhere.else.com@annen-nais-app")
        )

        cucumberTestsDto.initCucumberEnvironment()

        val restTjeneste = RestTjeneste("nais-app")
        val annenRestTjeneste = RestTjeneste("annen-nais-app")

        assertAll(
            { assertThat(restTjeneste.rest.baseUrl).`as`("tjeneste-app").isEqualTo("https://somewhere.com/nais-app") },
            { assertThat(annenRestTjeneste.rest.baseUrl).`as`("annen-tjeneste").isEqualTo("https://somewhere.else.com/annen-nais-app") }
        )
    }

    @Test
    fun `gitt INGRESSES_FOR_APPS med verdi for applikasjon (konfigurert som er en tag), skal RestTjeneste konfigureres med tag-navnet`() {
        val cucumberTestsDto = CucumberTestsDto(
            ingressesForApps = listOf("https://somewhere.com/@tag:nais-tag", "https://somewhere.else.com@tag:annen-nais-tag")
        )

        cucumberTestsDto.initCucumberEnvironment()

        val restTjeneste = RestTjeneste("nais-tag")
        val annenRestTjeneste = RestTjeneste("annen-nais-tag")

        assertAll(
            { assertThat(restTjeneste.rest.baseUrl).`as`("tjeneste-app").isEqualTo("https://somewhere.com/nais-tag") },
            { assertThat(annenRestTjeneste.rest.baseUrl).`as`("annen-tjeneste").isEqualTo("https://somewhere.else.com/annen-nais-tag") }
        )
    }

    @Test
    fun `skal hente full url uten advarsel`() {
        ScenarioManager.initCorrelationId()
        val restTemplateMock = mock(RestTemplate::class.java)
        val restTjeneste = RestTjeneste(RestTjeneste.ResttjenesteMedBaseUrl(restTemplateMock, "https://somewhere"))

        whenever(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String::class.java)))
            .thenReturn(ResponseEntity.ok().build())

        restTjeneste.exchangeGet("/out/there")

        val urlMedWarn = restTjeneste.hentFullUrlMedEventuellWarning()

        assertThat(urlMedWarn).`as`("full url").isEqualTo("https://somewhere/out/there")
    }

    @Test
    fun `skal ha WARNING fra HttpHeaders når rest tjeneste sender med dette som header`() {
        ScenarioManager.initCorrelationId()
        val restTemplateMock = mock(RestTemplate::class.java)
        val restTjeneste = RestTjeneste(RestTjeneste.ResttjenesteMedBaseUrl(restTemplateMock, "https://somewhere"))
        val headers = HttpHeaders(LinkedMultiValueMap(mapOf(HttpHeaders.WARNING to listOf("the truth will emerge!"))))

        whenever(restTemplateMock.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String::class.java)))
            .thenReturn(ResponseEntity.internalServerError().headers(headers).build())

        restTjeneste.exchangeGet("/out/there")

        val urlMedWarn = restTjeneste.hentFullUrlMedEventuellWarning()

        assertThat(urlMedWarn).`as`("url med warning fra header")
            .isEqualTo("https://somewhere/out/there - the truth will emerge!")
    }
}
