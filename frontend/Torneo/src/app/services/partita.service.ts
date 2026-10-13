import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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

  caricaPartiteTorneo(torneoId: number): void {
    this.http.get<Partita[]>(`${this.apiUrl}/torneo/${torneoId}`).subscribe({
      next: (data) => this.partite.set(data),
      error: (err) => console.error('Errore caricamento partite:', err)
    });
  }

  generaTabellone(torneoId: number, uid: number = 1): Observable<Partita[]> {
    return this.http.post<Partita[]>(`${this.apiUrl}/genera/${torneoId}?uid=${uid}`, {}).pipe(
      tap(data => this.partite.set(data))
    );
  }

  salvaRisultato(partitaId: number, punteggio1: number, punteggio2: number, stato: string = 'completata', uid?: number): Observable<Partita> {
    const queryUid = uid ?? 1;
    const payload = { punteggio1, punteggio2, stato };
    
    return this.http.put<Partita>(`${this.apiUrl}/${partitaId}/risultato?uid=${queryUid}`, payload).pipe(
      tap(partitaAggiornata => {
        this.partite.update(lista =>
          lista.map(p => p.id === partitaAggiornata.id ? partitaAggiornata : p)
        );
      })
    );
  }
}