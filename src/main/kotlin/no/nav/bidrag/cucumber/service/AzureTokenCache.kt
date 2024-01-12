package no.nav.bidrag.cucumber.service

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import com.nimbusds.oauth2.sdk.token.Tokens
import java.util.concurrent.TimeUnit

object AzureTokenCache {
    fun <T> accessTokenResponseCache(
        maximumSize: Long,
        skewInSeconds: Long,
    ): Cache<T, Tokens> {
        // Evict based on a varying expiration policy
        return Caffeine.newBuilder()
            .maximumSize(maximumSize)
            .expireAfter(evictOnResponseExpiresIn<Any>(skewInSeconds))
            .build()
    }

    private fun <T> evictOnResponseExpiresIn(skewInSeconds: Long): Expiry<T, Tokens> {
        return object : Expiry<T, Tokens> {
            override fun expireAfterCreate(
                p0: T,
                response: Tokens,
                p2: Long,
            ): Long {
                val seconds =
                    if (response.accessToken.lifetime > skewInSeconds) {
                        response.accessToken.lifetime - skewInSeconds
                    } else {
                        response.accessToken.lifetime
                    }
                return TimeUnit.SECONDS.toNanos(seconds)
            }

            override fun expireAfterUpdate(
                p0: T,
                p1: Tokens?,
                p2: Long,
                currentDuration: Long,
            ): Long {
                return currentDuration
            }

            override fun expireAfterRead(
                p0: T,
                p1: Tokens?,
                p2: Long,
                currentDuration: Long,
            ): Long {
                return currentDuration
            }
        }
    }
}
