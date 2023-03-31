package no.nav.bidrag.cucumber

import org.assertj.core.api.Assumptions.assumeThat
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

object TestUtil {

    fun assumeThatActuatorHealthIsRunning(ingress: String, contextPath: String) {
        val start = LocalDateTime.now()
        val future = CompletableFuture.runAsync {
            val response = RestTemplate().getForEntity("$ingress/$contextPath/actuator/health", String::class.java)

            assumeThat(response.statusCode).isEqualTo(HttpStatus.OK)
        }

        while (!future.isDone) {
            if (start.plusSeconds(1).isAfter(LocalDateTime.now())) {
                Thread.sleep(400)
            } else {
                assumeThat(HttpStatus.I_AM_A_TEAPOT).isEqualTo(HttpStatus.OK)
            }
        }
    }
}
