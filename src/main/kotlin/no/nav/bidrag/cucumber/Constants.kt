package no.nav.bidrag.cucumber

import no.nav.bidrag.cucumber.model.FilePath
import java.lang.Exception

// constants for input via System.getProperty(...)/System.getenv(...)
internal const val INGRESSES_FOR_APPS = "INGRESSES_FOR_APPS"
internal const val NO_CONTEXT_PATH_FOR_APPS = "NO_CONTEXT_PATH_FOR_APPS"
internal const val SECURITY_TOKEN = "SECURITY_TOKEN"
internal const val TAGS = "TAGS"
internal const val TEST_AUTH = "TEST_AUTH"
internal const val TEST_USER = "TEST_USER"
internal const val SANITY_CHECK = "SANITY_CHECK"

// spring configuration
internal const val PROFILE_LIVE = "LIVE"

internal val ABSOLUTE_CLOUD_PATH = FilePath("cloud-features.path").findFolderPath()
internal const val CORRELATION_ID = "correlationId"

internal const val FAGOMRADE_BIDRAG = "BID"

class AzureTokenException(message: String, exception: Exception? = null) : RuntimeException(message, exception)

fun usernameNotFound(): Nothing = throw RuntimeException("Fant ikke bruker")
