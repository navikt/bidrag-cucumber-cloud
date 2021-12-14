package no.nav.bidrag.cucumber.model

data class Assertion(val message: String, val value: Any?, val expectation: Any?, val verify: (input: Assertion) -> Unit) {
    fun doVerify() {
        try {
            verify(this)
        } catch (throwable: Throwable) {
            CucumberTestRun.holdExceptionForTest(throwable)
            throw throwable
        }
    }
}
