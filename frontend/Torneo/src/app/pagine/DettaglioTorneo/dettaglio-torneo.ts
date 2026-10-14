import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe, UpperCasePipe } from '@angular/common';

import { AutenticazioneMockService } from '../../services/autenticazione.service';
import { TorneiService } from '../../services/tornei.service';
import { PartiteService } from '../../services/partita.service';
import { Partita } from '../../interfaces/partita.interface';

export interface VoceClassifica {
  giocatoreId: number;
  nomeCompleto: string;
  vittorie: number;
  sconfitte: number;
  puntiFatti: number;
  puntiSubiti: number;
  differenzaPunti: number;
}

@Component({
  selector: 'app-dettaglio-torneo',
  standalone: true,
  imports: [RouterLink, DatePipe, UpperCasePipe],
  templateUrl: './dettaglio-torneo.html',
  styleUrl: './dettaglio-torneo.css'
})
export class DettaglioTorneo {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly autenticazioneService = inject(AutenticazioneMockService);
  private readonly torneiService = inject(TorneiService);
  private readonly partitaService = inject(PartiteService);

  readonly utenteCorrente = this.autenticazioneService.utenteCorrente;
  readonly torneoId = signal<number | null>(null);
  readonly partite = this.partitaService.partite;

 
  readonly partecipanti = signal<any[]>([]);

  readonly torneo = computed(() => {
    const id = this.torneoId();

    if (id === null || !Number.isInteger(id) || id <= 0) {
      return undefined;
    }

    return this.torneiService.tornei().find(t => t.id === id);
  });

  readonly giaIscritto = computed(() => {
    const torneo = this.torneo();
    const utente = this.utenteCorrente();

    if (!torneo || !utente) {
      return false;
    }

    const iscrittoNelProfilo =
      utente.torneiIscrittiIds?.includes(torneo.id) ?? false;

    const iscrittoTraPartecipanti =
      torneo.partecipantiIds?.includes(utente.id) ?? false;

    return iscrittoNelProfilo || iscrittoTraPartecipanti;
  });

  readonly partitePerTurno = computed(() => {
    const lista = this.partite();
    const mappa = new Map<number, Partita[]>();

    for (const p of lista) {
      const t = p.turno;
      if (!mappa.has(t)) {
        mappa.set(t, []);
      }
      mappa.get(t)!.push(p);
    }

    return Array.from(mappa.entries()).map(([turno, partite]) => ({
      turno,
      partite
    }));
  });

  readonly classifica = computed<VoceClassifica[]>(() => {
    const lista = this.partite();
    const mappaGiocatori = new Map<number, VoceClassifica>();

    const registraGiocatore = (id: number, nome: string, cognome: string) => {
      if (!mappaGiocatori.has(id)) {
        mappaGiocatori.set(id, {
          giocatoreId: id,
          nomeCompleto: `${nome} ${cognome}`,
          vittorie: 0,
          sconfitte: 0,
          puntiFatti: 0,
          puntiSubiti: 0,
          differenzaPunti: 0
        });
      }
    };

    for (const p of lista) {
      if (p.giocatore1) registraGiocatore(p.giocatore1.id, p.giocatore1.nome, p.giocatore1.cognome);
      if (p.giocatore2) registraGiocatore(p.giocatore2.id, p.giocatore2.nome, p.giocatore2.cognome);

      if (p.stato === 'completata') {
        const g1 = mappaGiocatori.get(p.giocatore1.id);
        const g2 = mappaGiocatori.get(p.giocatore2.id);

        const score1 = p.punteggio1 ?? 0;
        const score2 = p.punteggio2 ?? 0;

        if (g1) {
          g1.puntiFatti += score1;
          g1.puntiSubiti += score2;
          g1.differenzaPunti = g1.puntiFatti - g1.puntiSubiti;
          if (p.vincitoreId === g1.giocatoreId) g1.vittorie++;
          else if (p.vincitoreId && p.vincitoreId !== g1.giocatoreId) g1.sconfitte++;
        }

        if (g2) {
          g2.puntiFatti += score2;
          g2.puntiSubiti += score1;
          g2.differenzaPunti = g2.puntiFatti - g2.puntiSubiti;
          if (p.vincitoreId === g2.giocatoreId) g2.vittorie++;
          else if (p.vincitoreId && p.vincitoreId !== g2.giocatoreId) g2.sconfitte++;
        }
      }
    }

    return Array.from(mappaGiocatori.values()).sort((a, b) => {
      if (b.vittorie !== a.vittorie) return b.vittorie - a.vittorie;
      if (b.differenzaPunti !== a.differenzaPunti) return b.differenzaPunti - a.differenzaPunti;
      if (b.puntiFatti !== a.puntiFatti) return b.puntiFatti - a.puntiFatti;
      return 0;
    });
  });

  constructor() {
    if (this.torneiService.tornei().length === 0) {
      this.torneiService.caricaTornei();
    }

    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      const id = Number(idParam);

      if (idParam !== null && Number.isInteger(id) && id > 0) {
        this.torneoId.set(id);
        this.partitaService.caricaPartiteTorneo(id);
        
        this.caricaPartecipanti(id);
      } else {
        this.torneoId.set(null);
      }
    });
  }

  
  caricaPartecipanti(id: number): void {
    this.autenticazioneService.getPartecipantiTorneo(id).subscribe({
      next: (lista) => {
        this.partecipanti.set(lista);
      },
      error: (err) => {
        console.error('Errore durante il caricamento dei partecipanti:', err);
        this.partecipanti.set([]);
      }
    });
  }

  iscriviti(): void {
    const utente = this.utenteCorrente();
    const torneo = this.torneo();

    if (!utente) {
      this.router.navigate(['/login']);
      return;
    }

    if (!torneo) {
      alert('Torneo non trovato.');
      return;
    }

    if (this.giaIscritto()) {
      alert('Sei già iscritto a questo torneo.');
      return;
    }

    if (torneo.stato !== 'aperto') {
      alert('Le iscrizioni per questo torneo sono chiuse.');
      return;
    }

    if (torneo.iscritti >= torneo.postiMax) {
      alert('Il torneo ha raggiunto il numero massimo di partecipanti.');
      return;
    }

    this.autenticazioneService.aggiornaIscrizioniTornei(torneo.id, true).subscribe({
      next: (successo) => {
        if (successo) {
          this.torneiService.caricaTornei();
         
          this.caricaPartecipanti(torneo.id);
          alert('Iscrizione completata con successo!');
        } else {
          alert('Impossibile completare l\'iscrizione.');
        }
      },
      error: (err) => {
        console.error('Errore durante l\'iscrizione:', err);
        alert('Si è verificato un errore durante l\'iscrizione.');
      }
    });
  }

  disiscriviti(): void {
    const utente = this.utenteCorrente();
    const torneo = this.torneo();

    if (!utente || !torneo) {
      return;
    }

    if (!this.giaIscritto()) {
      alert('Non risulti iscritto a questo torneo.');
      return;
    }

    if (torneo.stato !== 'aperto') {
      alert('Non puoi annullare l\'iscrizione: il torneo non è più aperto.');
      return;
    }

    if (confirm('Sei sicuro di voler annullare la tua iscrizione?')) {
      this.autenticazioneService.aggiornaIscrizioniTornei(torneo.id, false).subscribe({
        next: (successo) => {
          if (successo) {
            this.torneiService.caricaTornei();
           
            this.caricaPartecipanti(torneo.id);
            alert('Iscrizione annullata.');
          } else {
            alert('Impossibile annullare l\'iscrizione.');
          }
        },
        error: (err) => {
          console.error('Errore durante la disiscrizione:', err);
          alert('Si è verificato un errore durante l\'annullamento.');
        }
      });
    }
  }

  esci(): void {
    this.autenticazioneService.esci();
    this.router.navigate(['/login']);
  }
}