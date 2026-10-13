import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';

import { AutenticazioneMockService } from '../../services/autenticazione.service';
import { TorneiService } from '../../services/tornei.service';

@Component({
  selector: 'app-tornei',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './tornei.html',
  styleUrl: './tornei.css'
})
export class Tornei {
  private readonly autenticazioneService = inject(AutenticazioneMockService);
  private readonly torneiService = inject(TorneiService);
  private readonly router = inject(Router);

  readonly utenteCorrente = this.autenticazioneService.utenteCorrente;
  readonly tuttiTornei = this.torneiService.tornei;

  readonly testoRicerca = signal<string>('');

  readonly statoSelezionato = signal<string>('tutti');
  readonly giocoSelezionato = signal<string>('tutti');
  readonly dataInizio = signal<string>('');
  readonly dataFine = signal<string>('');

  readonly giochiDisponibili = computed(() => {
    const giochi = this.tuttiTornei()
      .map(torneo => (torneo.gioco ?? '').trim())
      .filter(gioco => gioco.length > 0);

    return [...new Set(giochi)].sort((a, b) =>
      a.localeCompare(b, 'it', { sensitivity: 'base' })
    );
  });

  readonly torneiFiltrati = computed(() => {
    let tornei = this.tuttiTornei();

    const ricerca = this.testoRicerca().toLowerCase().trim();
    const stato = this.statoSelezionato();
    const gioco = this.giocoSelezionato();
    const inizio = this.dataInizio();
    const fine = this.dataFine();

    if (ricerca) {
      tornei = tornei.filter(torneo =>
        (torneo.nome ?? '').toLowerCase().includes(ricerca) ||
        (torneo.gioco ?? '').toLowerCase().includes(ricerca) ||
        (torneo.descrizione ?? '').toLowerCase().includes(ricerca)
      );
    }

    if (gioco !== 'tutti') {
      tornei = tornei.filter(torneo => torneo.gioco === gioco);
    }

    if (stato !== 'tutti') {
      tornei = tornei.filter(torneo => torneo.stato === stato);
    }

    if (inizio) {
      tornei = tornei.filter(torneo =>
        torneo.dataTorneo ? torneo.dataTorneo >= inizio : false
      );
    }

    if (fine) {
      tornei = tornei.filter(torneo =>
        torneo.dataTorneo ? torneo.dataTorneo <= fine : false
      );
    }

    return [...tornei].sort((a, b) =>
      (a.dataTorneo ?? '').localeCompare(b.dataTorneo ?? '')
    );
  });

  isIscritto(torneoId: number): boolean {
    return this.utenteCorrente()?.torneiIscrittiIds?.includes(torneoId) ?? false;
  }

  gestisciIscrizione(torneoId: number): void {
    if (!this.utenteCorrente()) {
      this.router.navigate(['/login']);
      return;
    }

    const iscritto = this.isIscritto(torneoId);
    this.autenticazioneService.aggiornaIscrizioniTornei(torneoId, !iscritto).subscribe({
      next: (successo) => {
        if (successo) {
          
          this.torneiService.caricaTornei();
        }
      },
      error: (err) => {
        console.error('Errore durante la gestione dell\'iscrizione:', err);
      }
    });
  }

  aggiornaRicerca(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.testoRicerca.set(input.value);
  }

  aggiornaStato(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.statoSelezionato.set(select.value);
  }

  aggiornaGioco(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.giocoSelezionato.set(select.value);
  }

  aggiornaDataInizio(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.dataInizio.set(input.value);
  }

  aggiornaDataFine(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.dataFine.set(input.value);
  }

  resetFiltri(): void {
    this.testoRicerca.set('');
    this.statoSelezionato.set('tutti');
    this.giocoSelezionato.set('tutti');
    this.dataInizio.set('');
    this.dataFine.set('');
  }

  esci(): void {
    this.autenticazioneService.esci();
    this.router.navigate(['/login']);
  }
}