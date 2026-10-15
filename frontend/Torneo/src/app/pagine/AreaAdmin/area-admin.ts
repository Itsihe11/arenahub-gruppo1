import { Component, effect, inject, signal , OnInit} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SlicePipe } from '@angular/common';

import { AutenticazioneMockService } from '../../services/autenticazione.service';
import { TorneiService, Torneo } from '../../services/tornei.service';
import { PartiteService } from '../../services/partita.service';

type StatoTorneo = 'aperto' | 'chiuso';

@Component({
  selector: 'app-area-admin',
  standalone: true,
  imports: [RouterLink, FormsModule, SlicePipe],
  templateUrl: './area-admin.html',
  styleUrl: './area-admin.css'
})
export class AreaAdmin implements OnInit {
  private readonly autenticazioneService = inject(AutenticazioneMockService);
  private readonly torneiService = inject(TorneiService);
  private readonly partitaService = inject(PartiteService);
  private readonly router = inject(Router);

  readonly utenteCorrente = this.autenticazioneService.utenteCorrente;
  readonly tuttiTornei = this.torneiService.tornei;
  readonly partite = this.partitaService.partite;

  readonly torneoSelezionato = signal<Torneo | null>(null);
  readonly modalitaModifica = signal(false);
  readonly partecipantiSelezionati = signal<any[]>([]);

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
  modificaStato: StatoTorneo = 'aperto';

  readonly limiteCorrente = this.torneiService.limiteTorneiPerGiocatore;
  readonly salvataggioLimiteInCorso = signal(false);
  readonly messaggioLimite = signal<{ tipo: 'ok' | 'errore'; testo: string } | null>(null);

  limiteInput = 3;

  constructor() {
    effect(() => {
      const limite = this.limiteCorrente();
      if (limite !== null) {
        this.limiteInput = limite;
      }
    });
  }
  ngOnInit(): void {
    this.torneiService.caricaTornei();
    this.torneiService.caricaLimite();
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
    }).subscribe({
      next: () => {
        alert('Torneo creato con successo!');
        this.nuovoNome = '';
        this.nuovoGioco = '';
        this.nuovaData = '';
        this.nuoviPostiMax = 16;
        this.nuovaDescrizione = '';
      },
      error: (err) => {
        console.error('Errore durante la creazione del torneo:', err);
        alert('Impossibile creare il torneo.');
      }
    });
  }

  
  selezionaTorneo(torneo: Torneo): void {
    this.torneoSelezionato.set(torneo);
    this.modalitaModifica.set(false);
    this.caricaDatiModifica(torneo);

    this.partitaService.caricaPartiteTorneo(torneo.id);
    this.caricaPartecipanti(torneo.id);
  }

  caricaPartecipanti(torneoId: number): void {
    this.partecipantiSelezionati.set([]);

    this.autenticazioneService.getPartecipantiTorneo(torneoId).subscribe({
      next: (lista) => {
        if (lista && lista.length > 0) {
          this.partecipantiSelezionati.set(lista);
        } else {
          this.estraiPartecipantiDaPartite();
        }
      },
      error: (err) => {
        console.error('Errore nel caricamento dei partecipanti admin:', err);
        this.estraiPartecipantiDaPartite();
      }
    });
  }

  private estraiPartecipantiDaPartite(): void {
    const mappa = new Map<string | number, any>();

    for (const p of this.partite()) {
      if (p.giocatore1) {
        const key = p.giocatore1.id || p.giocatore1.nome;
        if (key && !mappa.has(key)) {
          mappa.set(key, {
            id: p.giocatore1.id ?? key,
            nome: p.giocatore1.nome,
            cognome: p.giocatore1.cognome ?? ''
          });
        }
      }
      if (p.giocatore2) {
        const key = p.giocatore2.id || p.giocatore2.nome;
        if (key && !mappa.has(key)) {
          mappa.set(key, {
            id: p.giocatore2.id ?? key,
            nome: p.giocatore2.nome,
            cognome: p.giocatore2.cognome ?? ''
           
          });
        }
      }
    }

    this.partecipantiSelezionati.set(Array.from(mappa.values()));
  }

  private caricaDatiModifica(torneo: Torneo): void {
    this.modificaNome = torneo.nome;
    this.modificaGioco = torneo.gioco;
    this.modificaData = torneo.dataTorneo?.slice(0, 10) ?? '';
    this.modificaPostiMax = torneo.postiMax;
    this.modificaDescrizione = torneo.descrizione ?? '';
    this.modificaStato = torneo.stato === 'aperto' ? 'aperto' : 'chiuso';
  }

  attivaModifica(): void {
    const torneo = this.torneoSelezionato();
    if (!torneo) return;

    this.caricaDatiModifica(torneo);
    this.modalitaModifica.set(true);
  }

  annullaModifica(): void {
    this.modalitaModifica.set(false);
    const torneo = this.torneoSelezionato();
    if (torneo) {
      this.caricaDatiModifica(torneo);
    }
  }

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
      alert('Controlla i campi obbligatori e inserisci almeno 2 posti.');
      return;
    }

    const payload = {
      nome: this.modificaNome.trim(),
      gioco: this.modificaGioco.trim(),
      dataTorneo: this.modificaData,
      postiMax: this.modificaPostiMax,
      descrizione: this.modificaDescrizione.trim(),
      stato: this.modificaStato
    };

    this.torneiService.modificaTorneo(torneo.id, payload).subscribe({
      next: (risposta) => {
        this.torneoSelezionato.set({
          ...torneo,
          ...risposta,
          ...payload
        });
        this.modalitaModifica.set(false);
        alert('Modifiche salvate con successo!');
      },
      error: (err) => {
        console.error('Errore durante la modifica del torneo:', err);
        alert('Impossibile salvare le modifiche al torneo.');
      }
    });
  }

  cambiaStato(id: number, nuovoStato: StatoTorneo): void {
    this.torneiService.cambiaStatoTorneo(id, nuovoStato).subscribe({
      next: () => {
        const selezionato = this.torneoSelezionato();
        if (selezionato?.id === id) {
          this.torneoSelezionato.set({
            ...selezionato,
            stato: nuovoStato
          });
          this.modificaStato = nuovoStato;
        }
        alert(`Stato del torneo cambiato in "${nuovoStato}".`);
      },
      error: (err) => {
        console.error('Errore durante il cambio di stato:', err);
        alert('Impossibile aggiornare lo stato del torneo.');
      }
    });
  }

  elimina(id: number): void {
    if (!confirm('Sei sicuro di voler eliminare questo torneo?')) {
      return;
    }

    this.torneiService.eliminaTorneo(id).subscribe({
      next: () => {
        if (this.torneoSelezionato()?.id === id) {
          this.deselezionaTorneo();
        }
        alert('Torneo eliminato con successo!');
      },
      error: (err) => {
        console.error('Errore durante l\'eliminazione:', err);
        alert('Impossibile eliminare il torneo.');
      }
    });
  }

  deselezionaTorneo(): void {
    this.torneoSelezionato.set(null);
    this.modalitaModifica.set(false);
    this.partitaService.partite.set([]);
    this.partecipantiSelezionati.set([]);
  }

  generaPartite(): void {
    const torneo = this.torneoSelezionato();
    if (!torneo) return;

    const uid = this.utenteCorrente()?.id ?? 1;

    this.partitaService.generaTabellone(torneo.id, uid).subscribe({
      next: () => {
        alert('Tabellone generato con successo!');
        this.torneiService.caricaTornei();
        this.partitaService.caricaPartiteTorneo(torneo.id);
        setTimeout(() => this.caricaPartecipanti(torneo.id), 300);
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
      .salvaRisultato(partitaId, p1, p2, 'completata', uidCorrente)
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

  salvaLimite(): void {
    const valore = Number(this.limiteInput);

    if (!Number.isInteger(valore) || valore < 1) {
      this.messaggioLimite.set({
        tipo: 'errore',
        testo: 'Inserisci un numero intero maggiore di zero.'
      });
      return;
    }

    this.salvataggioLimiteInCorso.set(true);
    this.messaggioLimite.set(null);

    this.torneiService.salvaLimite(valore).subscribe({
      next: () => {
        this.torneiService.impostaLimiteLocale(valore);
        this.salvataggioLimiteInCorso.set(false);
        this.messaggioLimite.set({ tipo: 'ok', testo: 'Limite salvato.' });
      },
      error: err => {
        console.error('Errore durante il salvataggio del limite:', err);
        this.salvataggioLimiteInCorso.set(false);
        this.messaggioLimite.set({
          tipo: 'errore',
          testo: 'Impossibile salvare il limite. Il servizio non è ancora disponibile?'
        });
      }
    });
  }
}