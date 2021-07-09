package no.nav.bidrag.cucumber

// constants for input via System.getProperty(...)/System.getenv(...)
internal const val AZURE_APP_CLIENT_ID = "AZURE_APP_CLIENT_ID"
internal const val AZURE_APP_CLIENT_SECRET = "AZURE_APP_CLIENT_SECRET"
internal const val AZURE_APP_TENANT_ID = "AZURE_APP_TENANT_ID"
internal const val AZURE_LOGIN_ENDPOINT = "https://login.microsoftonline.com"
internal const val INGRESSES_FOR_TAGS = "INGRESSES_FOR_TAGS"
internal const val TEST_AUTH = "TEST_AUTH"
internal const val TEST_USER = "TEST_USER"
internal const val SANITY_CHECK = "SANITY_CHECK"

internal val ABSOLUTE_CLOUD_PATH = FilePath("cloud-features.path").findFolderPath()
