import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { UpperCasePipe } from '@angular/common';

import { AutenticazioneMockService } from '../../services/autenticazione.service';
import { TorneiService, Torneo } from '../../services/tornei.service';
import { PartiteService } from '../../services/partita.service';
import { Utente } from '../../interfaces/utente.interface';

@Component({
  selector: 'app-area-admin',
  standalone: true,
  imports: [RouterLink, FormsModule, UpperCasePipe],
  templateUrl: './area-admin.html',
  styleUrl: './area-admin.css'
})
export class AreaAdmin {
  private readonly autenticazioneService = inject(AutenticazioneMockService);
  private readonly torneiService = inject(TorneiService);
  private readonly partitaService = inject(PartiteService);
  private readonly router = inject(Router);

  readonly utenteCorrente = this.autenticazioneService.utenteCorrente;
  readonly tuttiTornei = this.torneiService.tornei;
  readonly partite = this.partitaService.partite;

  readonly torneoSelezionato = signal<Torneo | null>(null);
  readonly modalitaModifica = signal(false);

  nuovoNome = '';
  nuovoGioco = '';
  nuovaData = '';
  nuoviPostiMax = 16;
  nuovaDescrizione = '';

  modificaNome = '';
  modificaGioco = '';
  modificaData = '';
  modificaPostiMax = 16;
  modificaDescrizione = '';
  modificaStato: 'aperto' | 'chiuso' = 'aperto';

  get partecipantiSelezionati(): Utente[] {
    const torneo = this.torneoSelezionato();

    if (!torneo?.partecipantiIds?.length) {
      return [];
    }

    return torneo.partecipantiIds
      .map(id => this.getUtenteById(id))
      .filter((utente): utente is Utente => utente !== undefined);
  }

  getUtenteById(id: number): Utente | undefined {
    try {
      const utenti: Utente[] = JSON.parse(
        localStorage.getItem('mock_utenti') || '[]'
      );

      return utenti.find(utente => utente.id === id);
    } catch {
      return undefined;
    }
  }

  creaTorneo(): void {
    if (
      !this.nuovoNome.trim() ||
      !this.nuovoGioco.trim() ||
      !this.nuovaData ||
      this.nuoviPostiMax < 2
    ) {
      alert('Compila i campi obbligatori e inserisci almeno 2 posti.');
      return;
    }

    this.torneiService.creaTorneo({
      nome: this.nuovoNome.trim(),
      gioco: this.nuovoGioco.trim(),
      dataTorneo: this.nuovaData,
      postiMax: this.nuoviPostiMax,
      descrizione: this.nuovaDescrizione.trim()
    });

    this.nuovoNome = '';
    this.nuovoGioco = '';
    this.nuovaData = '';
    this.nuoviPostiMax = 16;
    this.nuovaDescrizione = '';
  }

  selezionaTorneo(torneo: Torneo): void {
    this.torneoSelezionato.set(torneo);
    this.modalitaModifica.set(false);

    this.modificaNome = torneo.nome;
    this.modificaGioco = torneo.gioco;
    this.modificaData = torneo.dataTorneo?.slice(0, 10) ?? '';
    this.modificaPostiMax = torneo.postiMax;
    this.modificaDescrizione = torneo.descrizione ?? '';
    this.modificaStato = torneo.stato === 'aperto' ? 'aperto' : 'chiuso';

    this.partitaService.caricaPartiteTorneo(torneo.id);
  }

  attivaModifica(): void {
    const torneo = this.torneoSelezionato();

    if (!torneo) {
      return;
    }

    this.modificaNome = torneo.nome;
    this.modificaGioco = torneo.gioco;
    this.modificaData = torneo.dataTorneo?.slice(0, 10) ?? '';
    this.modificaPostiMax = torneo.postiMax;
    this.modificaDescrizione = torneo.descrizione ?? '';
    this.modificaStato = torneo.stato === 'aperto' ? 'aperto' : 'chiuso';

    this.modalitaModifica.set(true);
  }

  annullaModifica(): void {
    this.modalitaModifica.set(false);

    const torneo = this.torneoSelezionato();

    if (torneo) {
      this.modificaNome = torneo.nome;
      this.modificaGioco = torneo.gioco;
      this.modificaData = torneo.dataTorneo?.slice(0, 10) ?? '';
      this.modificaPostiMax = torneo.postiMax;
      this.modificaDescrizione = torneo.descrizione ?? '';
      this.modificaStato = torneo.stato === 'aperto' ? 'aperto' : 'chiuso';
    }
  }

  salvaModificheTorneo(): void {
    const torneo = this.torneoSelezionato();

    if (!torneo) {
      alert('Seleziona prima un torneo da modificare.');
      return;
    }

    const datiAggiornati = {
      nome: this.nuovoNome,
      gioco: this.nuovoGioco,
      dataTorneo: this.nuovaData,
      postiMax: this.nuoviPostiMax,
      descrizione: this.nuovaDescrizione,
      stato: torneo.stato
    };

    this.torneiService.modificaTorneo(torneo.id, datiAggiornati);
  }


  cambiaStato(id: number, nuovoStato: 'aperto' | 'chiuso'): void {
    this.torneiService.cambiaStatoTorneo(id, nuovoStato);

    const selezionato = this.torneoSelezionato();

    if (selezionato?.id === id) {
      this.torneoSelezionato.set({
        ...selezionato,
        stato: nuovoStato
      });

      this.modificaStato = nuovoStato;
    }
  }

  elimina(id: number): void {
    if (!confirm('Sei sicuro di voler eliminare questo torneo?')) {
      return;
    }

    this.torneiService.eliminaTorneo(id);

    if (this.torneoSelezionato()?.id === id) {
      this.torneoSelezionato.set(null);
      this.modalitaModifica.set(false);
    }
  }

  deselezionaTorneo(): void {
    this.torneoSelezionato.set(null);
    this.modalitaModifica.set(false);
    this.partitaService.partite.set([]);
  }

  generaPartite(): void {
    const torneo = this.torneoSelezionato();

    if (!torneo) {
      return;
    }

    const uid = this.utenteCorrente()?.id ?? 1;

    this.partitaService.generaTabellone(torneo.id, uid).subscribe({
      next: () => {
        alert('Tabellone generato con successo!');
        this.torneiService.caricaTornei();
        this.partitaService.caricaPartiteTorneo(torneo.id);
      },
      error: err => {
        const messaggio =
          typeof err?.error === 'string'
            ? err.error
            : err?.error?.message ||
            'Impossibile generare il tabellone.';

        alert(messaggio);
      }
    });
  }

  salvaRisultatoPartita(
    partitaId: number,
    p1Val: string | number,
    p2Val: string | number
  ): void {
    const p1 = Number(p1Val);
    const p2 = Number(p2Val);
    const uidCorrente = this.utenteCorrente()?.id;

    if (
      p1Val === '' ||
      p2Val === '' ||
      !Number.isFinite(p1) ||
      !Number.isFinite(p2) ||
      p1 < 0 ||
      p2 < 0
    ) {
      alert('Inserisci punteggi validi e non negativi per entrambi i giocatori.');
      return;
    }

    this.partitaService
      .salvaRisultato(
        partitaId,
        p1,
        p2,
        'completata',
        uidCorrente
      )
      .subscribe({
        next: () => {
          alert('Risultato della partita salvato!');
          this.torneiService.caricaTornei();

          const torneo = this.torneoSelezionato();

          if (torneo) {
            this.partitaService.caricaPartiteTorneo(torneo.id);
          }
        },
        error: err => {
          console.error('Errore durante il salvataggio:', err);

          const messaggio =
            typeof err?.error === 'string'
              ? err.error
              : err?.error?.message ||
              'Impossibile salvare il risultato.';

          alert('Errore: ' + messaggio);
        }
      });
  }

  esci(): void {
    this.autenticazioneService.esci();
    this.router.navigate(['/login']);
  }
}