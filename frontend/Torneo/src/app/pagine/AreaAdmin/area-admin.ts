import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { UpperCasePipe, SlicePipe } from '@angular/common';

import { AutenticazioneMockService } from '../../services/autenticazione.service';
import { TorneiService, Torneo } from '../../services/tornei.service';
import { PartiteService } from '../../services/partita.service';
import { Utente } from '../../interfaces/utente.interface';

type StatoTorneo = 'aperto' | 'chiuso' | 'concluso';

@Component({
  selector: 'app-area-admin',
  standalone: true,
  imports: [RouterLink, FormsModule, UpperCasePipe, SlicePipe],
  templateUrl: './area-admin.html',
  styleUrl: './area-admin.css'
})
export class AreaAdmin {
  private readonly autenticazioneService =
    inject(AutenticazioneMockService);

  private readonly torneiService = inject(TorneiService);
  private readonly partitaService = inject(PartiteService);
  private readonly router = inject(Router);

  readonly utenteCorrente = this.autenticazioneService.utenteCorrente;
  readonly tuttiTornei = this.torneiService.tornei;
  readonly partite = this.partitaService.partite;

  readonly torneoSelezionato = signal<Torneo | null>(null);
  readonly modalitaModifica = signal(false);

  // Campi per creare un nuovo torneo
  nuovoNome = '';
  nuovoGioco = '';
  nuovaData = '';
  nuoviPostiMax = 16;
  nuovaDescrizione = '';

  // Campi per modificare il torneo selezionato
  modificaNome = '';
  modificaGioco = '';
  modificaData = '';
  modificaPostiMax = 16;
  modificaDescrizione = '';
  modificaStato: StatoTorneo = 'aperto';

  // Partecipanti presenti nei dati mock salvati nel browser
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

  // CREA TORNEO
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

  // SELEZIONA UN TORNEO
  selezionaTorneo(torneo: Torneo): void {
    this.torneoSelezionato.set(torneo);
    this.modalitaModifica.set(false);
    this.caricaDatiModifica(torneo);

    this.partitaService.caricaPartiteTorneo(torneo.id);
  }

  private caricaDatiModifica(torneo: Torneo): void {
    this.modificaNome = torneo.nome;
    this.modificaGioco = torneo.gioco;
    this.modificaData = torneo.dataTorneo?.slice(0, 10) ?? '';
    this.modificaPostiMax = torneo.postiMax;
    this.modificaDescrizione = torneo.descrizione ?? '';
    this.modificaStato = torneo.stato;
  }

  // ATTIVA LA MODIFICA
  attivaModifica(): void {
    const torneo = this.torneoSelezionato();

    if (!torneo) {
      return;
    }

    this.caricaDatiModifica(torneo);
    this.modalitaModifica.set(true);
  }

  // ANNULLA LA MODIFICA
  annullaModifica(): void {
    this.modalitaModifica.set(false);

    const torneo = this.torneoSelezionato();

    if (torneo) {
      this.caricaDatiModifica(torneo);
    }
  }

  // SALVA LE MODIFICHE
  salvaModificheTorneo(): void {
    const torneo = this.torneoSelezionato();

    if (!torneo) {
      alert('Seleziona prima un torneo da modificare.');
      return;
    }

    if (
      !this.modificaNome.trim() ||
      !this.modificaGioco.trim() ||
      !this.modificaData ||
      this.modificaPostiMax < 2
    ) {
      alert('Compila i campi obbligatori e inserisci almeno 2 posti.');
      return;
    }

    const datiAggiornati: Partial<Torneo> = {
      nome: this.modificaNome.trim(),
      gioco: this.modificaGioco.trim(),
      dataTorneo: this.modificaData,
      postiMax: this.modificaPostiMax,
      descrizione: this.modificaDescrizione.trim(),
      stato: this.modificaStato
    };

    this.torneiService.modificaTorneo(torneo.id, datiAggiornati);

    // Aggiorna subito la visualizzazione locale
    this.torneoSelezionato.set({
      ...torneo,
      ...datiAggiornati
    });

    this.modalitaModifica.set(false);
  }

  // CAMBIA STATO
  cambiaStato(id: number, nuovoStato: StatoTorneo): void {
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

  // ELIMINA TORNEO
  elimina(id: number): void {
    if (!confirm('Sei sicuro di voler eliminare questo torneo?')) {
      return;
    }

    this.torneiService.eliminaTorneo(id);

    if (this.torneoSelezionato()?.id === id) {
      this.torneoSelezionato.set(null);
      this.modalitaModifica.set(false);
      this.partitaService.partite.set([]);
    }
  }

  // CHIUDE LA GESTIONE DEL TORNEO
  deselezionaTorneo(): void {
    this.torneoSelezionato.set(null);
    this.modalitaModifica.set(false);
    this.partitaService.partite.set([]);
  }

  // GENERA IL TABELLONE
  generaPartite(): void {
    const torneo = this.torneoSelezionato();

    if (!torneo) {
      return;
    }

    const uid = this.utenteCorrente()?.id;

    if (uid == null) {
      alert('Non è stato possibile identificare l’amministratore.');
      return;
    }

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

  // SALVA IL RISULTATO DI UNA PARTITA
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
      alert(
        'Inserisci punteggi validi e non negativi per entrambi i giocatori.'
      );
      return;
    }

    if (uidCorrente == null) {
      alert('Non è stato possibile identificare l’amministratore.');
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

  // USCITA
  esci(): void {
    this.autenticazioneService.esci();
    this.router.navigate(['/login']);
  }
}