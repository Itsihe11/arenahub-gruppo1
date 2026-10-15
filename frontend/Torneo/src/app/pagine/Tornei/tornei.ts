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

  readonly immaginiGiochi: Record<string, string> = {
    'ea sports fc 24': 'https://www.playstationbit.com/wp-content/uploads/2023/07/EA-SPORTS-FC-24.jpg',
    'ea fc 24': 'https://www.playstationbit.com/wp-content/uploads/2023/07/EA-SPORTS-FC-24.jpg',
    'tekken 8': 'https://gaming-cdn.com/images/products/9579/616x353/tekken-8-pc-gioco-steam-cover.jpg?v=1744296954',
    'super smash bros. ultimate': 'https://images.unsplash.com/photo-1566577134770-3d85bb3a9cc4?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=NHwxMjA3fDA%3D',
    'street fighter 6': 'https://www.streetfighter.com/6/assets/images/index/mv_edition_bg.jpg',
    'streefighter v': 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSHVPfB2b2D4FhED08FbJZeHdo4gnzWPxF14VgGJ9agfedpuY_ecI37Ibw&s=10',
    'scacchi': 'https://pbs.twimg.com/media/HUO9T_3WcAAswOA?format=webp&name=large',
    'trackmania': 'https://store-images.s-microsoft.com/image/apps.12601.13929807647902137.38a53ce6-26b2-46d3-a4f5-54b363f7efcd.5a51a75a-b7d2-4138-8be9-9ce92ae4b837'
  };

   readonly placeholderGioco = 'https://images.ctfassets.net/s5n2t79q9icq/4ejLQCX5Y9E05jq96QoFLR/2e59320ed34130c2e92716c8ac69ac98/en_articles_archive_news_raising-new-banner-2017-10-08-meta-image.jpeg';


  getImmagineGioco(nomeGioco: string | undefined | null): string {
    if (!nomeGioco) return this.placeholderGioco;
    const chiave = nomeGioco.trim().toLowerCase();
    return this.immaginiGiochi[chiave] || this.placeholderGioco;
  }

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