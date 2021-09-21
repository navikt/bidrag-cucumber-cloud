package no.nav.bidrag.cucumber.model

import io.cucumber.java8.Scenario

class RunStats {
    private val exceptionMessages: MutableList<String> = ArrayList()
    private val failedScenarios: MutableList<String> = ArrayList()
    private var passed = 0
    private var total = 0

    fun add(scenario: Scenario) {
        total = total.inc()

        if (scenario.isFailed) {
            val namelessScenario = scenario.name == null || scenario.name.isBlank()

            failedScenarios.add("${scenario.uri} # ${if (namelessScenario) "Nameless" else scenario.name}")
        } else {
            passed = passed.inc()
        }
    }

    fun get(): String {
        val noOfFailed = failedScenarios.size
        val failedScenariosString = createFailedScenariosString()
        val failureDetailesString = createFaiureDetailesString()

        return """
            Scenarios: $total
            Passed   : $passed
            Failed   : $noOfFailed $failedScenariosString $failureDetailesString"""
    }

    private fun createFailedScenariosString() = if (failedScenarios.isEmpty()) "" else "\n\nFailed scenarios:\n${
        failedScenarios.joinToString(prefix = "- ", separator = "\n- ", postfix = "\n")
    }"

    private fun createFaiureDetailesString() = if (exceptionMessages.isEmpty()) "" else
        "\n\nFailure details!\n${exceptionMessages.joinToString(prefix = "- ", separator = "\n- ", postfix = "\n")}"

    fun addExceptionLogging(messages: List<String>) {
        exceptionMessages.addAll(messages)
    }

    override fun toString(): String {
        return get()
    }
}
