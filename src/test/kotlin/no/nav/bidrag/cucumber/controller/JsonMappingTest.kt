package no.nav.bidrag.cucumber.controller

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.cucumber.BidragCucumberCloudLocal
import no.nav.bidrag.cucumber.model.CucumberTestsDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [BidragCucumberCloudLocal::class])
@DisplayName("Test of mapping dto from json")
class JsonMappingTest {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `skal mappe en kjøring av bidrag-arberdsflyt`() {
        val json = """
          {
            "tags":["@bidrag-arbeidsflyt"],
            "testUsername":"z992903",
            "noContextPathForApps":["oppgave"],
            "ingressesForApps":["https://oppgave-q1.dev-fss-pub.nais.io@no-tag:oppgave"]
          }
          """.trimIndent()

        val cucumberTestsDto = objectMapper.readValue(json, CucumberTestsDto::class.java)

        assertAll(
            { assertThat(cucumberTestsDto).`as`("cucumberTestsDto").isNotNull() },
            { assertThat(cucumberTestsDto.tags).`as`("tags").isEqualTo(listOf("@bidrag-arbeidsflyt")) },
            { assertThat(cucumberTestsDto.testUsername).`as`("testUsername").isEqualTo("z992903") },
            { assertThat(cucumberTestsDto.noContextPathForApps).`as`("noContextPathForApps").isEqualTo(listOf("oppgave")) },
            {
                assertThat(cucumberTestsDto.ingressesForApps).`as`("ingressesForApps")
                    .isEqualTo(listOf("https://oppgave-q1.dev-fss-pub.nais.io@no-tag:oppgave"))
            }
        )
    }
}
