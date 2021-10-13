package no.nav.bidrag.cucumber.cloud.arbeidsflyt

import io.cucumber.java8.No
import io.cucumber.java8.PendingException
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService
import no.nav.bidrag.cucumber.cloud.FellesEgenskaperService.Assertion
import no.nav.bidrag.cucumber.hendelse.Hendelse
import org.assertj.core.api.Assertions.assertThat
import org.slf4j.LoggerFactory

@Suppress("unused") // brukes av cucumber
class ArbeidsflytEgenskaper : No {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(ArbeidsflytEgenskaper::class.java)
    }

    private lateinit var enhetsnummer: String
    private lateinit var hendelse: Hendelse
    private lateinit var tema: String
    private var journalpostId: Long = -1

    init {
        Og("hendelse {string} for journalpostId {long} og tema {string}") { hendelse: String, journalpostId: Long, tema: String ->
            this.hendelse = Hendelse.valueOf(hendelse)
            this.journalpostId = journalpostId
            this.tema = tema
        }

        Og("at det finnes en oppgave under behandling") {
            OppgaveOgHendelseService.tilbyOppgave(journalpostId = journalpostId, tema = tema, enhetsnummer = enhetsnummer)
        }

        Når("hendelsen opprettes for endring av fagområde til {string}") { tilFagomrade: String ->
            OppgaveOgHendelseService.opprettJournalpostHendelse(
                hendelse = hendelse,
                detaljer = mapOf("gammeltFagomrade" to tema, "nyttFagomrade" to tilFagomrade, "enhetsnummer" to enhetsnummer),
                journalpostId = "$tema-$journalpostId"
            )
        }

        Gitt("en oppgave for journalpostId {long} under tema {string} som tilhører enhet {string}") { journalpostId: Long, tema: String, enhetsnummer: String ->
            this.enhetsnummer = enhetsnummer
            this.journalpostId = journalpostId
            this.tema = tema
        }

        Gitt("at jeg søker etter oppgaven") {
            OppgaveOgHendelseService.sokOppgaveForHendelse(journalpostId = journalpostId, tema = tema)
        }

        Gitt("hendelsen opprettes for overføring til enhet {string}") { tilEnhet: String ->
            OppgaveOgHendelseService.opprettJournalpostHendelse(
                hendelse = hendelse,
                detaljer = mapOf("fagomrade" to tema, "gammeltEnhetsnummer" to enhetsnummer, "nyttEnhetsnummer" to tilEnhet),
                journalpostId = "$tema-$journalpostId"
            )
        }

        Og("jeg søker etter oppgaven") {
            OppgaveOgHendelseService.sokOppgaveForHendelse(journalpostId = journalpostId, tema = tema)
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
            OppgaveOgHendelseService.ferdigstillEventuellOppgave(journalpostId = journalpostId, tema = tema)
        }

        Når("hendelsen opprettes") {
            OppgaveOgHendelseService.opprettJournalpostHendelse(hendelse = hendelse, journalpostId = "$tema-$journalpostId")
        }

        Og("jeg venter i et sekund slik at hendelse blir behandlet") {
            LOGGER.info("Venter i et sekund slik at hendelse blir behandlet")
            Thread.sleep(1000)
        }

        Og("hendelsen gjelder enhet {string}") { enhetsnummer: String ->
            this.enhetsnummer = enhetsnummer
        }
    }
}
