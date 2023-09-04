package no.nav.bidrag.cucumber.model

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import no.nav.bidrag.cucumber.BidragCucumberCloudLocal
import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.service.AzureTokenService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@SpringBootTest(classes = [BidragCucumberCloudLocal::class])
@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
internal class RestTjenesteTest {

    @MockkBean
    private lateinit var azureTokenService: AzureTokenService

    @BeforeEach
    fun `reset Cucumber environment`() {
        Environment.reset()
    }

    @BeforeEach
    fun `stub azure security`() {
        val oaut2AuthorizedClientMock: OAuth2AuthorizedClient = mockk("OAuth2AuthorizedClient")
        val oauth2AccessTokenMock: OAuth2AccessToken = mockk("OAuth2AccessToken")

        every {  azureTokenService.generateToken(any(), any())} returns "token"
        every { azureTokenService.generateToken(any(), isNull()) } returns ""
        every { azureTokenService.getToken(any(), isNull()) } returns ""
        every {oaut2AuthorizedClientMock.accessToken } returns oauth2AccessTokenMock
        every {oauth2AccessTokenMock.tokenValue } returns "my secured token"
    }

    @Test
    fun `gitt INGRESSES_FOR_APPS med verdi for applikasjon, skal RestTjeneste konfigureres med denne verdien`() {
        val cucumberTestsModel = CucumberTestsModel(
            ingressesForApps = listOf("https://somewhere.com/@nais-app", "https://somewhere.else.com@annen-nais-app"),
            testUsername = "James Bond"
        )

        cucumberTestsModel.initCucumberEnvironment()

        val restTjeneste = RestTjeneste.konfigurerResttjeneste("nais-app")
        val annenRestTjeneste = RestTjeneste.konfigurerResttjeneste("annen-nais-app")

        assertAll(
            { assertThat(restTjeneste.rest.baseUrl).`as`("tjeneste-app").isEqualTo("https://somewhere.com/nais-app") },
            { assertThat(annenRestTjeneste.rest.baseUrl).`as`("annen-tjeneste").isEqualTo("https://somewhere.else.com/annen-nais-app") }
        )
    }

    @Test
    fun `gitt INGRESSES_FOR_APPS med verdi for applikasjon (konfigurert som er en tag), skal RestTjeneste konfigureres med tag-navnet`() {
        val cucumberTestsModel = CucumberTestsModel(
            ingressesForApps = listOf("https://somewhere.com/@nais-tag", "https://somewhere.else.com@annen-nais-tag"),
            testUsername = "James Bond"
        )

        cucumberTestsModel.initCucumberEnvironment()

        val restTjeneste = RestTjeneste.konfigurerResttjeneste("nais-tag")
        val annenRestTjeneste = RestTjeneste.konfigurerResttjeneste("annen-nais-tag")

        assertAll(
            { assertThat(restTjeneste.rest.baseUrl).`as`("tjeneste-app").isEqualTo("https://somewhere.com/nais-tag") },
            { assertThat(annenRestTjeneste.rest.baseUrl).`as`("annen-tjeneste").isEqualTo("https://somewhere.else.com/annen-nais-tag") }
        )
    }

    @Test
    fun `skal hente full url uten advarsel`() {
        val restTemplateMock: RestTemplate = mockk("RestTemplate")
        val restTjeneste = RestTjeneste(ResttjenesteMedBaseUrl(restTemplateMock, "https://somewhere"))

        every { restTemplateMock.exchange(any<String>(), eq(HttpMethod.GET), any(), eq(String::class.java)) } returns ResponseEntity.ok().build()

        restTjeneste.exchangeGet("/out/there")

        val urlMedWarn = restTjeneste.hentFullUrlMedEventuellWarning()

        assertThat(urlMedWarn).`as`("full url").isEqualTo("https://somewhere/out/there")
    }

    @Test
    fun `skal ha WARNING fra HttpHeaders når rest tjeneste sender med dette som header`() {
        val restTemplateMock: RestTemplate = mockk("RestTemplate")
        val restTjeneste = RestTjeneste(ResttjenesteMedBaseUrl(restTemplateMock, "https://somewhere"))
        val headers = HttpHeaders(LinkedMultiValueMap(mapOf(HttpHeaders.WARNING to listOf("the truth will emerge!"))))
        every { restTemplateMock.exchange(any<String>(), eq(HttpMethod.GET), any(), eq(String::class.java)) } returns ResponseEntity.internalServerError().headers(headers).build()

        restTjeneste.exchangeGet("/out/there")

        val urlMedWarn = restTjeneste.hentFullUrlMedEventuellWarning()

        assertThat(urlMedWarn).`as`("url med warning fra header")
            .isEqualTo("https://somewhere/out/there - the truth will emerge!")
    }
}
