package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import io.cucumber.java8.No
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService.Assertion
import no.nav.bidrag.cucumber.model.JournalpostHendelse
import org.assertj.core.api.Assertions.assertThat
import org.slf4j.LoggerFactory

@Suppress("unused") // brukes av cucumber
class ArbeidsflytEgenskaper : No {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(ArbeidsflytEgenskaper::class.java)
    }

    private lateinit var journalpostHendelse: JournalpostHendelse

    init {
        Og("en journalpostHendelse med journalpostId {long} og fagområde {string}") { journalpostId: Long, fagomrade: String ->
            journalpostHendelse = JournalpostHendelse(
                journalpostId = "$fagomrade-$journalpostId",
                enhet = "4806",
                fagomrade = fagomrade
            )
        }

        Og("at det finnes en oppgave under behandling") {
            OppgaveOgHendelseService.tilbyOppgave(journalpostHendelse = journalpostHendelse)
        }

        Og("at det finnes en oppgave under behandling for enhet {string}") { enhetsnummer: String ->
            journalpostHendelse.enhet = enhetsnummer
            OppgaveOgHendelseService.tilbyOppgave(journalpostHendelse = journalpostHendelse)
        }

        Når("hendelsen opprettes med fagområde {string}") { fagomrade: String ->
            journalpostHendelse.fagomrade = fagomrade
            OppgaveOgHendelseService.opprettJournalpostHendelse(journalpostHendelse)
        }

        Gitt("en oppgave for denne hendelsen som tilhører enhet {string}") { enhetsnummer: String ->
            journalpostHendelse.enhet = enhetsnummer
        }

        Gitt("at jeg søker etter oppgaven") {
            OppgaveOgHendelseService.sokOppgaveForHendelse(
                journalpostId = journalpostHendelse.hentJournalpostIdUtenPrefix(),
                tema = journalpostHendelse.fagomrade!!
            )
        }

        Gitt("hendelsen opprettes med enhet {string}") { enhetsnummer: String ->
            journalpostHendelse.enhet = enhetsnummer
            OppgaveOgHendelseService.opprettJournalpostHendelse(journalpostHendelse)
        }

        Og("jeg søker etter oppgaven på fagområde {string}") { fagomrade: String ->
            OppgaveOgHendelseService.sokOppgaveForHendelse(journalpostId = journalpostHendelse.hentJournalpostIdUtenPrefix(), tema = fagomrade)
        }

        Så("skal jeg finne oppgaven i søkeresultatet") {
            FellesEgenskaperService.assertWhenNotSanityCheck(
                Assertion(
                    message = "Forventet å finne oppgaven",
                    value = FellesEgenskaperService.hentRestTjeneste().hentResponseSomMap()["antallTreffTotalt"],
                    expectation = 1,
                    verify = { assertion: Assertion -> assertThat(assertion.value).`as`(assertion.message).isEqualTo(assertion.expectation) }
                ),
            )
        }

        Så("skal jeg finne oppgaven i søkeresultatet med enhet {string}") { enhetsnummer: String ->
            OppgaveOgHendelseService.assertThatOppgaveTilhorerEnhet(enhetsnummer)
        }

        Så("skal jeg ikke finne oppgaven i søkeresultatet") {
            FellesEgenskaperService.assertWhenNotSanityCheck(
                Assertion(
                    message = "Forventet ikke å finne oppgaven",
                    value = FellesEgenskaperService.hentRestTjeneste().hentResponseSomMap()["antallTreffTotalt"],
                    expectation = 0,
                    verify = { assertion: Assertion -> assertThat(assertion.value).`as`(assertion.message).isEqualTo(assertion.expectation) }
                ),
            )
        }

        Og("at det ikke finnes en åpen oppgave") {
            OppgaveOgHendelseService.ferdigstillEventuellOppgave(
                journalpostId = journalpostHendelse.hentJournalpostIdUtenPrefix(),
                tema = journalpostHendelse.fagomrade!!
            )
        }

        Når("hendelsen opprettes") {
            OppgaveOgHendelseService.opprettJournalpostHendelse(journalpostHendelse)
        }

        Og("jeg venter i et sekund slik at hendelse blir behandlet") {
            LOGGER.info("Venter i et sekund slik at hendelse blir behandlet")
            Thread.sleep(1000)
        }

        Og("hendelsen gjelder enhet {string}") { enhetsnummer: String ->
            journalpostHendelse.enhet = enhetsnummer
        }
    }
}
