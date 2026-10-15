import { Injectable, signal, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { tap, map, catchError } from 'rxjs/operators';
import { Utente } from '../interfaces/utente.interface';

@Injectable({
  providedIn: 'root'
})
export class AutenticazioneMockService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api';

  private readonly chiaveUtenteLoggato = 'utente_loggato';
  private readonly chiaveToken = 'auth_token';

  readonly utenteCorrente = signal<Utente | null>(
    this.leggiUtenteLoggato()
  );

  readonly token = signal<string | null>(
    localStorage.getItem(this.chiaveToken)
  );

  constructor() {
   
    if (this.utenteCorrente()) {
      this.sincronizzaUtenteEIscrizioni();
    }
  }

  private leggiUtenteLoggato(): Utente | null {
    try {
      const dato = localStorage.getItem(this.chiaveUtenteLoggato);
      return dato ? this.normalizzaUtente(JSON.parse(dato)) : null;
    } catch {
      return null;
    }
  }


  private normalizzaUtente(raw: any): Utente {
    if (!raw) return raw;

  
    const src = raw.utente ?? raw.user ?? raw;

    const idEstratto = Number(src.id ?? src.uid ?? src.idUtente);

    const ruoloRaw = String(src.ruolo ?? src.role ?? 'utente').toLowerCase();
    const ruoloNormalizzato = ruoloRaw.includes('admin') ? 'admin' : 'utente';

    return {
      ...src,
      id: isNaN(idEstratto) ? src.id : idEstratto,
      nome: src.nome ?? src.nomeUtente ?? src.first_name ?? '',
      cognome: src.cognome ?? src.last_name ?? '',
      email: src.email ?? '',
      ruolo: ruoloNormalizzato,
      torneiIscrittiIds: Array.isArray(raw.torneiIscrittiIds ?? src.torneiIscrittiIds)
        ? (raw.torneiIscrittiIds ?? src.torneiIscrittiIds).map((id: any) => Number(id))
        : []
    };
  }

  private creaHeaders(): HttpHeaders {
    const t = this.token() || localStorage.getItem(this.chiaveToken) || '';
    const authHeader = t ? (t.startsWith('Bearer ') ? t : `Bearer ${t}`) : '';
    return new HttpHeaders({
      'Authorization': authHeader
    });
  }

  private salvaSessione(rispostaGrezza: any, tokenRicevuto?: string): void {
    const utenteObj = rispostaGrezza?.utente ?? rispostaGrezza?.user ?? rispostaGrezza;
    const utente = this.normalizzaUtente(utenteObj);

    const tokenGenerato = tokenRicevuto
      ?? rispostaGrezza?.token
      ?? rispostaGrezza?.tokenJwt
      ?? `token-${utente.id}-${Date.now()}`;

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
      tap(risposta => {
        if (risposta) {
          this.salvaSessione(risposta, risposta.token || risposta.tokenJwt);
          
          this.sincronizzaUtenteEIscrizioni();
        }
      }),
      map(risposta => !!risposta),
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

    return this.http.post<any>(`${this.apiUrl}/registrazione`, payload).pipe(
      tap(risposta => {
        if (risposta) {
          this.salvaSessione(risposta, risposta.token || risposta.tokenJwt);
        }
      }),
      map(risposta => !!risposta),
      catchError(err => {
        console.error('Errore durante la registrazione:', err);
        return of(false);
      })
    );
  }

 
  sincronizzaUtenteEIscrizioni(): void {
    const utenteAttuale = this.utenteCorrente();
    if (!utenteAttuale) return;

    const headers = this.creaHeaders();

    this.http.get<any>(`${this.apiUrl}/utenti/me`, { headers }).pipe(
      tap(datiMe => {
        if (datiMe) {
          const profiloAggiornato = this.normalizzaUtente({
            ...utenteAttuale,
            ...datiMe
          });
          this.utenteCorrente.set(profiloAggiornato);
          localStorage.setItem(this.chiaveUtenteLoggato, JSON.stringify(profiloAggiornato));
        }
      }),
      catchError(err => {
        console.warn('Impossibile recuperare /api/utenti/me:', err);
        return of(null);
      })
    ).subscribe();

    this.http.get<any[]>(`${this.apiUrl}/utenti/me/iscrizioni`, { headers }).pipe(
      tap(listaIscrizioni => {
        const ids = Array.isArray(listaIscrizioni)
          ? listaIscrizioni.map(item => Number(item.torneoId ?? item.idTorneo ?? item.id))
          : [];

        const utente = this.utenteCorrente();
        if (utente) {
          const utenteConIscrizioni = this.normalizzaUtente({
            ...utente,
            torneiIscrittiIds: ids
          });
          this.utenteCorrente.set(utenteConIscrizioni);
          localStorage.setItem(this.chiaveUtenteLoggato, JSON.stringify(utenteConIscrizioni));
        }
      }),
      catchError(err => {
        console.error('Errore caricamento iscrizioni:', err);
        return of([]);
      })
    ).subscribe();
  }

  aggiornaIscrizioniTornei(torneoId: number | string, iscrivi: boolean): Observable<boolean> {
    const utente = this.utenteCorrente();
    if (!utente) return of(false);

    const targetId = Number(torneoId);
    if (isNaN(targetId)) {
      return throwError(() => new Error('ID Torneo non valido.'));
    }

    const headers = this.creaHeaders();
    const url = `${this.apiUrl}/tornei/${targetId}/iscrizione`;

    const richiesta$ = iscrivi
      ? this.http.post(url, {}, { headers })
      : this.http.delete(url, { headers });

    return richiesta$.pipe(
      tap(() => {
        const idsAttuali = (utente.torneiIscrittiIds ?? []).map(id => Number(id));
        let nuoviIds: number[];

        if (iscrivi) {
          nuoviIds = Array.from(new Set([...idsAttuali, targetId]));
        } else {
          nuoviIds = idsAttuali.filter(id => id !== targetId);
        }

        const utenteAggiornato = this.normalizzaUtente({
          ...utente,
          torneiIscrittiIds: nuoviIds
        });

        this.utenteCorrente.set(utenteAggiornato);
        localStorage.setItem(this.chiaveUtenteLoggato, JSON.stringify(utenteAggiornato));
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

  getPartecipantiTorneo(torneoId: number | string): Observable<any[]> {
    const id = Number(torneoId);
    const headers = this.creaHeaders();

    return this.http.get<any>(`${this.apiUrl}/tornei/${id}/partecipanti`, { headers }).pipe(
      map(risposta => {
        let listaGrezza: any[] = [];

     
        if (Array.isArray(risposta)) {
          listaGrezza = risposta;
        } else if (risposta && typeof risposta === 'object') {
          listaGrezza = (
            risposta.partecipanti ||
            risposta.iscritti ||
            risposta.utenti ||
            risposta.lista ||
            risposta.data ||
            []
          );
        }

        
        return listaGrezza.map(item => {
          const u = item.utente ?? item.user ?? item;
          return {
            id: Number(u.id ?? item.idUtente ?? item.utenteId ?? item.id),
            nome: u.nome ?? u.nomeUtente ?? u.first_name ?? item.nomeUtente ?? item.nome ?? '',
            cognome: u.cognome ?? u.last_name ?? item.cognome ?? '',
            email: u.email ?? item.email ?? ''
          };
        });
      }),
      catchError(err => {
        console.error('Errore nel caricamento dei partecipanti:', err);
        return of([]);
      })
    );
  }

  caricaIscrizioniUtente(): Observable<number[]> {
  const u = this.utenteCorrente();
  if (!u) return of([]);

  const headers = this.creaHeaders();
  return this.http.get<any[]>('http://localhost:8080/api/utenti/me/iscrizioni', { headers }).pipe(
    map(iscrizioni => iscrizioni.map(i => Number(i.id))),
    tap(ids => {
      const utenteAggiornato = this.normalizzaUtente({
        ...u,
        torneiIscrittiIds: ids
      });
      this.utenteCorrente.set(utenteAggiornato);
      localStorage.setItem(this.chiaveUtenteLoggato, JSON.stringify(utenteAggiornato));
    }),
    catchError(err => {
      console.error('Errore nel recupero iscrizioni utente dal backend:', err);
      return of(u.torneiIscrittiIds || []);
    })
  );
}
  
}