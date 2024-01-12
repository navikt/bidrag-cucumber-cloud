package no.nav.bidrag.cucumber.cloud

import no.nav.bidrag.cucumber.model.CucumberTestRun

class TestdataManager {
    companion object {
        fun erstattUrlMedParametereFraTestdata(url: String): String {
            if (url.contains("{") && url.contains("}")) {
                val firstIndex = url.indexOf("{")
                val secondIndex = url.indexOf("}", firstIndex)
                val key = url.substring(firstIndex + 1, secondIndex)
                val verdi = CucumberTestRun.thisRun().testData.hentData()[key] as String
                return erstattUrlMedParametereFraTestdata(url.replaceRange(firstIndex, secondIndex + 1, verdi))
            }
            return url
        }

        fun erstattJsonMedParametereFraTestdata(json: String): String {
            if (json.contains("\${") && json.contains("}")) {
                val firstIndex = json.indexOf("\${")
                val secondIndex = json.indexOf("}", firstIndex)
                val key = json.substring(firstIndex + 2, secondIndex)
                val verdi = CucumberTestRun.thisRun().testData.hentData()[key] as String
                return erstattJsonMedParametereFraTestdata(json.replaceRange(firstIndex, secondIndex + 1, verdi))
            }
            return json
        }
    }
}
