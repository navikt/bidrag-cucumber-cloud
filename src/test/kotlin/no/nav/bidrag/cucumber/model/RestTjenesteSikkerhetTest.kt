package no.nav.bidrag.cucumber.model

import no.nav.bidrag.commons.web.HttpHeaderRestTemplate
import no.nav.bidrag.cucumber.BidragCucumberCloudLocal
import no.nav.bidrag.cucumber.Environment
import no.nav.bidrag.cucumber.dto.CucumberTestsApi
import no.nav.bidrag.cucumber.service.AzureTokenService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest(classes = [BidragCucumberCloudLocal::class])
internal class RestTjenesteSikkerhetTest {

    @MockBean
    private lateinit var azureTokenServiceMock: AzureTokenService

    @MockBean
    private lateinit var httpHeaderRestTemplateMock: HttpHeaderRestTemplate

    @BeforeEach
    fun `reset Environment`() {
        Environment.resetCucumberEnvironment()
    }

    @Test
    fun `skal generere AZURE token ved kall med url`() {
        CucumberTestsModel(
            CucumberTestsApi(
                ingressesForApps = listOf("https://somewhere@nais-app"),
                testUsername = "jactor-rises"
            )
        ).initCucumberEnvironment()

        RestTjeneste("nais-app")
        verify(azureTokenServiceMock).generateBearerToken("nais-app")
    }
}