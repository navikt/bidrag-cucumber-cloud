package no.nav.bidrag.junit.cucumber

import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith

/**
 * Runner class which looks for feature-files (in src/test/resources/no.nav.bidrag.cucumber.*) to test
 */
@RunWith(Cucumber::class)
@CucumberOptions(plugin = ["pretty", "json:target/cucumber-report/cucumber.json"])
class RunCucumberTest