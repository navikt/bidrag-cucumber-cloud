package no.nav.bidrag.cucumber.model

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.cucumber.BidragCucumberCloudLocal
import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.service.AzureTokenService
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.core.OAuth2AccessToken

@SpringBootTest(classes = [BidragCucumberCloudLocal::class])
internal class RestTjenesteSikkerhetTest {

    @MockBean
    private lateinit var azureTokenServiceMock: AzureTokenService

    @MockBean
    private lateinit var httpHeaderRestTemplateMock: HttpHeaderRestTemplate

    @MockBean
    private lateinit var oAuth2AuthorizedClientManagerMock: OAuth2AuthorizedClientManager

    @BeforeEach
    fun `reset Cucumber environment`() {
        Environment.reset()
    }

    @BeforeEach
    fun `stub azure security`() {
        val oaut2AuthorizedClientMock = Mockito.mock(OAuth2AuthorizedClient::class.java)
        val oauth2AccessTokenMock = Mockito.mock(OAuth2AccessToken::class.java)

        whenever(oAuth2AuthorizedClientManagerMock.authorize(any())).thenReturn(oaut2AuthorizedClientMock)
        whenever(oaut2AuthorizedClientMock.accessToken).thenReturn(oauth2AccessTokenMock)
    }

//    @Test
//    fun `skal generere AZURE token når RestTjeneste blir konfigurert`() {
//        whenever(azureTokenServiceMock.cacheGeneratedToken("nais-app")).thenReturn("secured token")
//        CucumberTestsModel(
//            CucumberTestsApi(
//                ingressesForApps = listOf("https://somewhere@nais-app"),
//                testUsername = "jactor-rises"
//            )
//        ).initCucumberEnvironment()
//
//        RestTjeneste.konfigurerResttjeneste("nais-app")
//
//        val tokenCaptor = ArgumentCaptor.forClass(ValueGenerator::class.java)
//        verify(httpHeaderRestTemplateMock).addHeaderGenerator(eq(HttpHeaders.AUTHORIZATION), tokenCaptor.capture())
//        val tokenValue = tokenCaptor.value.generate()
//
//        assertThat(tokenValue).isEqualTo("Bearer secured token")
//    }
}