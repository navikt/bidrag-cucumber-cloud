package no.nav.bidrag.cucumber.sikkerhet

import no.nav.bidrag.cucumber.AZURE_APP_CLIENT_ID
import no.nav.bidrag.cucumber.AZURE_APP_CLIENT_SECRET
import no.nav.bidrag.cucumber.TEST_AUTH
import no.nav.bidrag.cucumber.TEST_USER
import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity

internal class TokenProviderTest {
    @Test
    @Suppress("NonAsciiCharacters")
     fun `gitt at kjøremiljøet har miljøvariabler for azure, skal man hente Azure token`() {
        System.setProperty(AZURE_APP_CLIENT_ID, "xyz")
        System.setProperty(AZURE_APP_CLIENT_SECRET, "hemmelig")
        System.setProperty("TEST_AUTH_JACTOR-RISES", "hysj")
        System.setProperty(TEST_USER, "jactor-rises")

        val providerMock = mock(TokenProvider.Provider::class.java)
        val tokenProvider = TokenProvider(providerMock)

        whenever(providerMock.postForEntity(anyString(), any())).thenReturn(ResponseEntity.ok(Token("abc", "type", 20)))

        tokenProvider.fetchAzureToken("https://login.com")

        verify(providerMock).postForEntity(
            eq("https://login.com"),
            any()
        )
    }
}