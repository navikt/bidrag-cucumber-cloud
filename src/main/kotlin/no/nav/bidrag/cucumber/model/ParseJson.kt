package no.nav.bidrag.cucumber.model

import com.jayway.jsonpath.JsonPath

fun parseJson(response: String?, sti: String): String? {
    if (response == null) {
        return null
    }

    val documentContext = JsonPath.parse(response)
    return documentContext.read<Any>(sti).toString()
}
