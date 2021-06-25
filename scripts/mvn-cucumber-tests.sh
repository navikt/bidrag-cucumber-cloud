mvn exec:java                                  \
    -Dexec.classpathScope=test                 \
    -Dexec.mainClass=io.cucumber.core.cli.Main \
    -Dexec.args="src/test/resources/no/nav/bidrag/cucumber/cloud --glue no.nav.bidrag.cucumber.cloud"
