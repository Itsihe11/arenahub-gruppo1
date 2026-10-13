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

  nuovoNome = '';
  nuovoGioco = '';
  nuovaData = '';
  nuoviPostiMax = 16;
  nuovaDescrizione = '';

  getUtenteById(id: number): Utente | undefined {
    const utenti: Utente[] = JSON.parse(localStorage.getItem('mock_utenti') || '[]');
    return utenti.find(u => u.id === id);
  }

  creaTorneo(): void {
    if (!this.nuovoNome || !this.nuovoGioco || !this.nuovaData) return;

    this.torneiService.creaTorneo({
      nome: this.nuovoNome,
      gioco: this.nuovoGioco,
      dataTorneo: this.nuovaData,
      postiMax: this.nuoviPostiMax,
      descrizione: this.nuovaDescrizione
    });

    this.nuovoNome = '';
    this.nuovoGioco = '';
    this.nuovaData = '';
    this.nuovaDescrizione = '';
  }

  cambiaStato(id: number, nuovoStato: 'aperto' | 'chiuso' | 'concluso'): void {
    this.torneiService.cambiaStatoTorneo(id, nuovoStato);
  }

  elimina(id: number): void {
    if (confirm('Sei sicuro di voler eliminare questo torneo?')) {
      this.torneiService.eliminaTorneo(id);
      if (this.torneoSelezionato()?.id === id) {
        this.torneoSelezionato.set(null);
      }
    }
  }

  selezionaTorneo(torneo: Torneo): void {
    this.torneoSelezionato.set(torneo);
   
    this.partitaService.caricaPartiteTorneo(torneo.id);
  }

  deselezionaTorneo(): void {
    this.torneoSelezionato.set(null);
  }

generaPartite(): void {
    const torneo = this.torneoSelezionato();
    if (!torneo) return;

    this.partitaService.generaTabellone(torneo.id).subscribe({
      next: () => {
        alert('Nuovo turno generato con successo!');
        this.torneiService.caricaTornei();
      },
      error: (err) => {
        const messaggio = typeof err?.error === 'string'
          ? err.error
          : (err?.error?.message || 'Impossibile generare il prossimo turno.');

        alert(messaggio);

  
        this.torneiService.caricaTornei();
      }
    });
  }

salvaRisultatoPartita(partitaId: number, p1Val: string | number, p2Val: string | number): void {
    const p1 = Number(p1Val);
    const p2 = Number(p2Val);
    const uidCorrente = this.utenteCorrente()?.id;

    if (isNaN(p1) || isNaN(p2)) {
      alert('Inserisci punteggi validi per entrambi i giocatori.');
      return;
    }

    this.partitaService.salvaRisultato(partitaId, p1, p2, 'completata', uidCorrente).subscribe({
      next: () => {
        alert('Risultato partita salvato!');
        
        this.torneiService.caricaTornei();
      },
      error: (err) => {
        console.error('Errore durante il salvataggio:', err);
        const messaggio = typeof err?.error === 'string'
          ? err.error
          : (err?.error?.message || 'Impossibile salvare il risultato.');
        alert('Errore: ' + messaggio);
      }
    });
  }

  esci(): void {
    this.autenticazioneService.esci();
    this.router.navigate(['/login']);
  }
}