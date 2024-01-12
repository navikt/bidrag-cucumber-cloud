package no.nav.bidrag.cucumber.sikkerhet

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint

@Configuration
@EnableWebSecurity
class SecurityConfiguration(
    @Value("\${REST_AUTH_BRUKERNAVN}") val authBrukernavn: String,
    @Value("\${REST_AUTH_PASSORD}") val authPassord: String,
) {
    @Bean
    fun userDetailsService(): UserDetailsService {
        val user: UserDetails =
            User
                .withUsername(authBrukernavn)
                .password("{noop}$authPassord").roles("USER")
                .build()
        return InMemoryUserDetailsManager(user)
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
                auth.anyRequest().authenticated()
            }
            .httpBasic { it.authenticationEntryPoint(Http403ForbiddenEntryPoint()) }
            .csrf { it.disable() }
        return http.build()
    }
}
