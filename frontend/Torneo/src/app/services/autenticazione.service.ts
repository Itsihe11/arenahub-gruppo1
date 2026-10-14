import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { tap, map, catchError } from 'rxjs/operators';
import { Utente } from '../interfaces/utente.interface';

@Injectable({
  providedIn: 'root'
})
export class AutenticazioneMockService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api';
  private readonly iscrizioniUrl = 'http://localhost:8080/api/iscrizioni';

  private readonly chiaveUtenteLoggato = 'utente_loggato';
  private readonly chiaveToken = 'auth_token';

  readonly utenteCorrente = signal<Utente | null>(
    this.leggiUtenteLoggato()
  );

  readonly token = signal<string | null>(
    localStorage.getItem(this.chiaveToken)
  );

  private leggiUtenteLoggato(): Utente | null {
    try {
      const dato = localStorage.getItem(this.chiaveUtenteLoggato);
      return dato ? (JSON.parse(dato) as Utente) : null;
    } catch {
      return null;
    }
  }

  private salvaSessione(utente: Utente, tokenRicevuto?: string): void {
    const tokenGenerato = tokenRicevuto ?? `token-${utente.id}-${Date.now()}`;

    this.utenteCorrente.set(utente);
    this.token.set(tokenGenerato);

    localStorage.setItem(this.chiaveUtenteLoggato, JSON.stringify(utente));
    localStorage.setItem(this.chiaveToken, tokenGenerato);
  }

  accedi(email: string, password: string): Observable<boolean> {
  const payload = {
    email: email.trim().toLowerCase(),
    password
  };

  return this.http.post<any>(`${this.apiUrl}/login`, payload).pipe(
    tap(res => {
      
      const utente: Utente = res.utente ?? res.user ?? res;
      const token = res.token;

      if (utente) {
        this.salvaSessione(utente, token);
      }
    }),
    map(res => !!res),
    catchError(err => {
      console.error('Errore durante il login:', err);
      return of(false);
    })
  );
}

  registra(
    nome: string,
    cognome: string,
    email: string,
    password: string
  ): Observable<boolean> {
    const payload = {
      nome: nome.trim(),
      cognome: cognome.trim(),
      email: email.trim().toLowerCase(),
      password
    };

    return this.http.post<Utente>(`${this.apiUrl}/registrazione`, payload).pipe(
      tap(utente => {
        if (utente) {
          this.salvaSessione(utente);
        }
      }),
      map(utente => !!utente),
      catchError(err => {
        console.error('Errore durante la registrazione:', err);
        return of(false);
      })
    );
  }

  aggiornaIscrizioniTornei(torneoId: number | string, iscrivi: boolean): Observable<boolean> {
    const utente = this.utenteCorrente();
    if (!utente) return of(false);

    const targetId = Number(torneoId);
    

    const rawUserId = utente.id ?? (utente as any).uid ?? (utente as any).idUtente;
    const userId = Number(rawUserId);

    if (isNaN(userId) || isNaN(targetId)) {
      console.error('ID Utente o ID Torneo non valido:', { rawUserId, targetId, utente });
      return throwError(() => new Error('ID Utente o ID Torneo non valido.'));
    }

    const endpoint = iscrivi
      ? `${this.iscrizioniUrl}/iscrivi`
      : `${this.iscrizioniUrl}/annulla`;

    const payload = {
      uid: userId,
      torneoId: targetId
    };

    return this.http.post(endpoint, payload).pipe(
      tap(() => {
        const idsAttuali = (utente.torneiIscrittiIds ?? []).map(id => Number(id));
        
        let nuoviIds: number[];
        if (iscrivi) {
          nuoviIds = Array.from(new Set([...idsAttuali, targetId]));
        } else {
          nuoviIds = idsAttuali.filter(id => id !== targetId);
        }

        const utenteAggiornato: Utente = {
          ...utente,
          torneiIscrittiIds: nuoviIds
        };

        this.utenteCorrente.set(utenteAggiornato);
        localStorage.setItem(
          this.chiaveUtenteLoggato,
          JSON.stringify(utenteAggiornato)
        );
      }),
      map(() => true),
      catchError(err => {
        console.error('Errore durante la gestione dell\'iscrizione:', err);
        
        return throwError(() => err);
      })
    );
  }

  esci(): void {
    this.utenteCorrente.set(null);
    this.token.set(null);

    localStorage.removeItem(this.chiaveUtenteLoggato);
    localStorage.removeItem(this.chiaveToken);
  }
}