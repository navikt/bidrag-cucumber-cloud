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

        Og("at det finnes en oppgave under behandling med oppgavetype {string}") { oppgavetype: String ->
            OppgaveOgHendelseService.tilbyOppgave(journalpostHendelse = journalpostHendelse, oppgavetype = oppgavetype)
        }

        Når("hendelsen opprettes med fagområde {string}") { fagomrade: String ->
            journalpostHendelse.fagomrade = fagomrade
            OppgaveOgHendelseService.opprettJournalpostHendelse(journalpostHendelse)
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

        Og("jeg søker etter oppgaver på fagområde {string}") { fagomrade: String ->
            OppgaveOgHendelseService.sokOppgaveForHendelse(journalpostId = journalpostHendelse.hentJournalpostIdUtenPrefix(), tema = fagomrade)
        }

        Så("skal jeg finne oppgave i søkeresultatet") {
            FellesEgenskaperService.assertWhenNotSanityCheck(
                Assertion(
                    message = "Forventet å finne oppgaven",
                    value = FellesEgenskaperService.hentRestTjeneste().hentResponseSomMap()["antallTreffTotalt"],
                    expectation = 1,
                    verify = { assertion: Assertion -> assertThat(assertion.value).`as`(assertion.message).isEqualTo(assertion.expectation) }
                ),
            )
        }

        Så("skal jeg finne oppgave i søkeresultatet med enhet {string}") { enhetsnummer: String ->
            OppgaveOgHendelseService.assertThatOppgaveTilhorer(enhet = enhetsnummer)
        }

        Så("skal jeg finne oppgave i søkeresultatet med oppgavetypen {string}") { oppgavetype: String ->
            OppgaveOgHendelseService.assertThatOppgaveTilhorer(oppgavetype = oppgavetype)
        }

        Så("skal jeg ikke finne oppgave i søkeresultatet") {
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

        Når("hendelsen opprettes med aktør id {string} og journalstatus {string}") { aktorId: String, journalstatus: String ->
            journalpostHendelse.aktorId = aktorId
            journalpostHendelse.journalstatus = journalstatus
            OppgaveOgHendelseService.opprettJournalpostHendelse(journalpostHendelse)
        }

        Når("hendelsen opprettes uten aktør id, men med journalstatus {string}") { journalstatus: String ->
            journalpostHendelse.journalstatus = journalstatus
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
