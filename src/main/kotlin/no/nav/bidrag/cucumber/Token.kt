package no.nav.bidrag.cucumber

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class Token(
    @JsonProperty(value = "access_token", required = true)
    val token: String,
    @JsonProperty(value = "token_type", required = true)
    val type: String,
    @JsonProperty(value = "expires_in", required = true)
    val expiresIn: Int
) {

    private val expirationTime: LocalDateTime = LocalDateTime.now().plusSeconds(expiresIn - 20L)

    fun hasExpired(): Boolean = expirationTime.isBefore(LocalDateTime.now())
}