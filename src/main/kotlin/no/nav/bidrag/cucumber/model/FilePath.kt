package no.nav.bidrag.cucumber.model

import org.slf4j.LoggerFactory
import java.io.File

data class FilePath(val fileName: String) {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(FilePath::class.java)
    }

    internal fun findFile() = File(".")
        .walkBottomUp()
        .filterNot { it.absolutePath.contains("/target/") }
        .filter { it.name.endsWith(".path") }
        .find { isFileName(it) } ?: throw IllegalStateException("Cannot find $fileName located in ${File(".").absolutePath}")

    private fun isFileName(file: File): Boolean {
        LOGGER.info("is $fileName?: $file")

        return fileName == file.name
    }

    fun findFolderPath() = File(findFile().parent)
        .absolutePath.replace("/./", "/")
}
