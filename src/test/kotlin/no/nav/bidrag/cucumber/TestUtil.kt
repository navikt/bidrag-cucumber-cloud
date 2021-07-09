package no.nav.bidrag.cucumber

import org.assertj.core.api.Assumptions.assumeThat
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate

object TestUtil {

    fun assumeThatActuatorHealthIsRunning(ingress: String, contextPath: String) {
        val response = RestTemplate().getForEntity("$ingress/$contextPath/actuator/health", String::class.java)

        assumeThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }
}