package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.dto.CucumberTestsApi
import no.nav.bidrag.cucumber.model.CucumberTestsModel
import no.nav.bidrag.cucumber.service.AzureTokenService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest(classes = [BidragCucumberCloudLocal::class])
internal class RestTjenesteSikkerhetTest {

    @MockBean
    private lateinit var azureTokenServiceMock: AzureTokenService

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

        val restTjeneste = RestTjeneste("nais-app")

        assertThrows<RuntimeException> { restTjeneste.exchangeGet("out-there") } // url eksisterer ikke...
        verify(azureTokenServiceMock).generateBearerToken("nais-app")
    }
}