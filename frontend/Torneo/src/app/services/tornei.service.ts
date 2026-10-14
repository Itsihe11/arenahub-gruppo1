import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AutenticazioneMockService } from './autenticazione.service';
import { Observable } from 'rxjs';

export interface Torneo {
  id: number;
  nome: string;
  gioco: string;
  descrizione: string;
  dataTorneo: string;
  postiMax: number;
  iscritti: number;
  stato: 'aperto' | 'chiuso' | 'concluso';
  partecipantiIds?: number[];
  classificaFinale?: {
    utenteId: number;
    posizione: number;
    punteggio: number;
  }[];
}

@Injectable({
  providedIn: 'root'
})
export class TorneiService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AutenticazioneMockService);
  private readonly apiUrl = 'http://localhost:8080/api/tornei';

  readonly tornei = signal<Torneo[]>([]);

  constructor() {
    this.caricaTornei();
  }

  caricaTornei(): void {
    this.http.get<Torneo[]>(this.apiUrl).subscribe({
      next: (data) => this.tornei.set(data),
      error: (err) => console.error('Errore nel caricamento dei tornei dal backend:', err)
    });
  }

  getTorneoById(id: number): Torneo | undefined {
    return this.tornei().find(t => t.id === id);
  }

  creaTorneo(
    nuovo: Omit<Torneo, 'id' | 'iscritti' | 'stato' | 'partecipantiIds' | 'classificaFinale'>
  ): void {
    const torneoPayload = {
      ...nuovo,
      stato: 'aperto'
    };

    this.http.post<Torneo>(this.apiUrl, torneoPayload).subscribe({
      next: () => this.caricaTornei(),
      error: (err) => console.error('Errore durante la creazione del torneo:', err)
    });
  }

  cambiaStatoTorneo(id: number, nuovoStato: 'aperto' | 'chiuso' | 'concluso'): void {
    const torneoCorrente = this.getTorneoById(id);
    if (!torneoCorrente) return;

    const torneoAggiornato = { ...torneoCorrente, stato: nuovoStato };

    this.http.put<Torneo>(`${this.apiUrl}/${id}`, torneoAggiornato).subscribe({
      next: () => this.caricaTornei(),
      error: (err) => console.error('Errore durante l\'aggiornamento dello stato:', err)
    });
  }

  eliminaTorneo(id: number): void {
    this.http.delete<void>(`${this.apiUrl}/${id}`).subscribe({
      next: () => {
        this.caricaTornei();
        this.authService.aggiornaIscrizioniTornei(id, false);
      },
      error: (err) => console.error('Errore durante l\'eliminazione del torneo:', err)
    });
  }


  modificaTorneo(id: number, dati: Partial<Torneo>): void {
    const uid = this.authService.utenteCorrente()?.id;

    if (uid == null) {
      console.error('Impossibile modificare il torneo: amministratore non identificato.');
      return;
    }

    this.http.put<Torneo>(`${this.apiUrl}/${id}?uid=${uid}`, dati).subscribe({
      next: () => {
        this.caricaTornei();
      },
      error: (err) => console.error('Errore durante la modifica del torneo:', err)
    });
  }



  iscriviUtente(torneoId: number, utenteId: number): void {
    this.http.post<void>(`http://localhost:8080/api/iscrizioni`, { torneoId, utenteId }).subscribe({
      next: () => {
        this.caricaTornei();
        this.aggiornaIscrizioneUtente(utenteId, torneoId, true);
      },
      error: (err) => console.error('Errore durante l\'iscrizione:', err)
    });
  }

  disiscriviUtente(torneoId: number, utenteId: number): void {
    this.http.delete<void>(`http://localhost:8080/api/iscrizioni?torneoId=${torneoId}&utenteId=${utenteId}`).subscribe({
      next: () => {
        this.caricaTornei();
        this.aggiornaIscrizioneUtente(utenteId, torneoId, false);
      },
      error: (err) => console.error('Errore durante la disiscrizione:', err)
    });
  }

  salvaRisultatoGiocatore(
    torneoId: number,
    utenteId: number,
    posizione: number,
    punteggio: number
  ): void {
    const payload = { torneoId, utenteId, posizione, punteggio };

    this.http.post<void>(`http://localhost:8080/api/partite/risultato`, payload).subscribe({
      next: () => this.caricaTornei(),
      error: (err) => console.error('Errore durante il salvataggio del risultato:', err)
    });
  }

  private aggiornaIscrizioneUtente(utenteId: number, torneoId: number, iscrivi: boolean): void {
    const utente = this.authService.utenteCorrente();
    if (!utente || utente.id !== utenteId) return;
    this.authService.aggiornaIscrizioniTornei(torneoId, iscrivi);
  }
}