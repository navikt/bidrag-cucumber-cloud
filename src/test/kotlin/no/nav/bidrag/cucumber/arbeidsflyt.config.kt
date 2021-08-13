package no.nav.bidrag.cucumber

import org.mockito.Mockito.mock
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate

@Configuration
@Profile("!$PROFILE_LIVE")
class NotLiveSpringConfig {

    @Suppress("UNCHECKED_CAST")
    fun kafkaTemplate(): KafkaTemplate<String, String> = mock(KafkaTemplate::class.java) as KafkaTemplate<String, String>
}
