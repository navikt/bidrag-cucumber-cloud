package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.hendelse.HendelseProducer
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
@Profile("!$PROFILE_LIVE")
class NotLiveSpringConfig {

    @Bean
    fun hendelseProducer() = mock(HendelseProducer::class.java)!!

    @Bean
    @Suppress("UNCHECKED_CAST")
    fun kafkaTemplate(): KafkaTemplate<String, String> = KafkaTemplate(mock(ProducerFactory::class.java)) as KafkaTemplate<String, String>
}
