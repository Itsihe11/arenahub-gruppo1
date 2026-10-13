import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { UpperCasePipe } from '@angular/common';

import { AutenticazioneMockService } from '../../services/autenticazione-mock.service';
import { TorneiService, Torneo } from '../../services/tornei.service';
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
  private readonly router = inject(Router);

  readonly utenteCorrente = this.autenticazioneService.utenteCorrente;
  readonly tuttiTornei = this.torneiService.tornei;

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
  }

  deselezionaTorneo(): void {
    this.torneoSelezionato.set(null);
  }

  salvaRisultato(torneoId: number, utenteId: number, posStr: string, puntiStr: string): void {
    const posizione = Number(posStr);
    const punteggio = Number(puntiStr);

    if (!posizione || isNaN(punteggio)) {
      alert('Inserisci una posizione valida e un punteggio.');
      return;
    }

    this.torneiService.salvaRisultatoGiocatore(torneoId, utenteId, posizione, punteggio);
    alert('Risultato salvato con successo!');
  }

  esci(): void {
    this.autenticazioneService.esci();
    this.router.navigate(['/login']);
  }
}