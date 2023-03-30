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

            failedScenarios.add("${scenario.uri} # ${if (namelessScenario) "Nameless in ${scenario.uri}" else scenario.name}")
        } else {
            passed = passed.inc()
        }
    }

    fun get(): String {
        val noOfFailed = failedScenarios.size
        val failedScenariosAsString = createStringOfFailedScenarios()
        val failureDetailsAsString = createStringOfFailureDetails()

        return """
            Scenarios: $total
            Passed   : $passed
            Failed   : $noOfFailed $failedScenariosAsString $failureDetailsAsString"""
    }

    private fun createStringOfFailedScenarios() = if (failedScenarios.isEmpty()) {
        ""
    } else {
        "\n\nFailed scenarios:\n${
        failedScenarios.joinToString(prefix = "- ", separator = "\n- ")
        }"
    }

    internal fun createStringOfFailureDetails() = if (exceptionMessages.isEmpty()) {
        ""
    } else {
        "\n\nFailure details:\n${exceptionMessages.joinToString(separator = "\n")}"
    }

    fun addExceptionLogging(messages: List<String>) {
        exceptionMessages.addAll(messages.mapIndexed { idx, message -> if (idx == 0) "- ${indentLines(message)}" else "  ${indentLines(message)}" })
    }

    private fun indentLines(message: String) = message.replace("\n", "\n  ")
}
