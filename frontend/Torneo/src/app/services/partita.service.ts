import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Partita } from '../interfaces/partita.interface';

@Injectable({
  providedIn: 'root'
})
export class PartiteService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/partite';

  readonly partite = signal<Partita[]>([]);


  private creaHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth_token') || '';
    const authHeader = token ? (token.startsWith('Bearer ') ? token : `Bearer ${token}`) : '';
    return new HttpHeaders({
      'Authorization': authHeader
    });
  }

  caricaPartiteTorneo(torneoId: number): void {
    const headers = this.creaHeaders();

    this.http.get<Partita[]>(`${this.apiUrl}/torneo/${torneoId}`, { headers }).subscribe({
      next: (data) => this.partite.set(data),
      error: (err) => console.error('Errore caricamento partite:', err)
    });
  }

  generaTabellone(torneoId: number, uid: number = 1): Observable<Partita[]> {
    const headers = this.creaHeaders();

    
    return this.http.post<Partita[]>(`${this.apiUrl}/genera/${torneoId}`, {}, { headers }).pipe(
      tap(data => this.partite.set(data))
    );
  }

  salvaRisultato(
    partitaId: number,
    punteggio1: number,
    punteggio2: number,
    stato: string = 'completata',
    uid?: number
  ): Observable<Partita> {
    const headers = this.creaHeaders();
    const payload = { punteggio1, punteggio2, stato };

    return this.http.put<Partita>(`${this.apiUrl}/${partitaId}/risultato`, payload, { headers }).pipe(
      tap(partitaAggiornata => {
        this.partite.update(lista =>
          lista.map(p => p.id === partitaAggiornata.id ? partitaAggiornata : p)
        );
      })
    );
  }
}