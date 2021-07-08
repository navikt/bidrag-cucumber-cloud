package no.nav.bidrag.cucumber

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.io.File

internal class FilePathTest {
    @Test
    fun `skal finne cloud-features i prosjekt katalogen`() {
        val filePath = FilePath("cloud-features.path")
        val pathFile = filePath.findFile()

        assertAll(
            { assertThat(pathFile.exists()).`as`("file exists").isTrue() },
            {
                assertThat(filePath.findFolderPath()).`as`("folder path")
                    .isEqualTo(File("src/main/resources/no/nav/bidrag/cucumber/cloud").absolutePath)
            },
        )
    }
}