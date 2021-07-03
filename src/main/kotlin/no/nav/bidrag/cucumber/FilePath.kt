package no.nav.bidrag.cucumber

import org.slf4j.LoggerFactory
import java.io.File

data class FilePath(val fileName: String) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(FilePath::class.java)
    }

    internal fun findFile() = File(".")
        .walkBottomUp()
        .filter { it.name == fileName }
        .filterNot { it.absolutePath.contains("/target/") }
        .find { isFileName(it) } ?: throw IllegalStateException("Cannot find $fileName located in ${File(".").absolutePath}")

    private fun isFileName(file: File): Boolean {
        LOGGER.info("is $fileName?: $file")

        return fileName == file.name
    }

    fun findFolderPath() = File(findFile().parent)
        .absolutePath.replace("/./", "/")
}
