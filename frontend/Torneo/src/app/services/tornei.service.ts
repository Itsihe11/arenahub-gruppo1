import { Injectable, signal, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AutenticazioneMockService } from './autenticazione.service';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface Torneo {
  id: number;
  nome: string;
  gioco: string;
  descrizione: string;
  dataTorneo: string;
  postiMax: number;
  iscritti: number;
  stato: 'aperto' | 'chiuso' | string;
  partecipantiIds?: number[];
  classificaFinale?: {
    utenteId: number;
    posizione: number;
    punteggio: number;
  }[];
}

export interface MaxTorneiResponse {
  maxTorneiAttivi: number;
}

export interface StatoIscrizioneResponse {
  torneiAttiviConteggiati: number;
  limiteMassimo: number;
  canRegister: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class TorneiService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AutenticazioneMockService);
  
  private readonly apiUrl = 'http://localhost:8080/api/tornei';
  private readonly adminMaxTorneiUrl = 'http://localhost:8080/api/admin/maxTornei';
  private readonly meStatoIscrizioneUrl = 'http://localhost:8080/api/utente/me/statoIscrizione';

  readonly tornei = signal<Torneo[]>([]);
  readonly limiteTorneiPerGiocatore = signal<number | null>(null);

  constructor() {
    this.caricaTornei();
    this.caricaLimite();
  }

  private creaHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth_token') || '';
    const authHeader = token ? (token.startsWith('Bearer ') ? token : `Bearer ${token}`) : '';
    return new HttpHeaders({
      'Authorization': authHeader,
      'Content-Type': 'application/json'
    });
  }

  caricaTornei(): void {
    const headers = this.creaHeaders();
    this.http.get<Torneo[]>(this.apiUrl, { headers }).subscribe({
      next: (data) => this.tornei.set(data || []),
      error: (err) => console.error('Errore nel caricamento dei tornei dal backend:', err)
    });
  }

  getTorneoById(id: number): Torneo | undefined {
    return this.tornei().find(t => t.id === id);
  }

  creaTorneo(
    nuovo: Omit<Torneo, 'id' | 'iscritti' | 'stato' | 'partecipantiIds' | 'classificaFinale'>
  ): Observable<Torneo> {
    const headers = this.creaHeaders();
    const torneoPayload = {
      ...nuovo,
      stato: 'aperto'
    };

    return this.http.post<Torneo>(this.apiUrl, torneoPayload, { headers }).pipe(
      tap(() => this.caricaTornei())
    );
  }

  cambiaStatoTorneo(id: number, nuovoStato: 'aperto' | 'chiuso'): Observable<Torneo> {
    const headers = this.creaHeaders();
    const torneoCorrente = this.getTorneoById(id);

    const payload = torneoCorrente
      ? { ...torneoCorrente, stato: nuovoStato }
      : { stato: nuovoStato };

    return this.http.put<Torneo>(`${this.apiUrl}/${id}`, payload, { headers }).pipe(
      tap(() => this.caricaTornei())
    );
  }

  modificaTorneo(id: number, dati: Partial<Torneo>): Observable<Torneo> {
    const headers = this.creaHeaders();

    return this.http.put<Torneo>(`${this.apiUrl}/${id}`, dati, { headers }).pipe(
      tap(() => this.caricaTornei())
    );
  }

  eliminaTorneo(id: number): Observable<void> {
    const headers = this.creaHeaders();
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers }).pipe(
      tap(() => {
        this.caricaTornei();
        this.authService.aggiornaIscrizioniTornei(id, false);
      })
    );
  }


  iscriviUtente(torneoId: number, utenteId: number): Observable<void> {
    const headers = this.creaHeaders();
    return this.http.post<void>(`http://localhost:8080/api/tornei/${torneoId}/iscrizione`, {}, { headers }).pipe(
      tap(() => {
        this.caricaTornei();
        this.aggiornaIscrizioneUtente(utenteId, torneoId, true);
      })
    );
  }

  
  disiscriviUtente(torneoId: number, utenteId: number): Observable<void> {
    const headers = this.creaHeaders();
    return this.http.delete<void>(`http://localhost:8080/api/tornei/${torneoId}/iscrizione`, { headers }).pipe(
      tap(() => {
        this.caricaTornei();
        this.aggiornaIscrizioneUtente(utenteId, torneoId, false);
      })
    );
  }

  salvaRisultatoGiocatore(
    torneoId: number,
    utenteId: number,
    posizione: number,
    punteggio: number
  ): Observable<void> {
    const headers = this.creaHeaders();
    const payload = { torneoId, utenteId, posizione, punteggio };

    return this.http.post<void>('http://localhost:8080/api/partite/risultato', payload, { headers }).pipe(
      tap(() => this.caricaTornei())
    );
  }

  caricaLimite(): void {
    const headers = this.creaHeaders();
    this.http.get<MaxTorneiResponse>(this.adminMaxTorneiUrl, { headers }).subscribe({
      next: (data) => this.limiteTorneiPerGiocatore.set(data.maxTorneiAttivi),
      error: (err) => console.warn('Limite tornei non disponibile:', err)
    });
  }

  salvaLimite(limite: number): Observable<MaxTorneiResponse> {
    const headers = this.creaHeaders();
    return this.http.put<MaxTorneiResponse>(
      this.adminMaxTorneiUrl,
      { maxTorneiAttivi: limite },
      { headers }
    );
  }

  getStatoIscrizioneUtente(): Observable<StatoIscrizioneResponse> {
    const headers = this.creaHeaders();
    return this.http.get<StatoIscrizioneResponse>(this.meStatoIscrizioneUrl, { headers });
  }

  impostaLimiteLocale(limite: number): void {
    this.limiteTorneiPerGiocatore.set(limite);
  }

  private aggiornaIscrizioneUtente(utenteId: number, torneoId: number, iscrivi: boolean): void {
    const utente = this.authService.utenteCorrente();
    if (!utente || utente.id !== utenteId) return;
    this.authService.aggiornaIscrizioniTornei(torneoId, iscrivi);
  }
}