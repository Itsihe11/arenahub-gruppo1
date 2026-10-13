
import { Injectable, signal } from '@angular/core';
import { Utente } from '../interfaces/utente.interface';

@Injectable({
  providedIn: 'root'
})
export class AutenticazioneMockService {
  private readonly chiaveUtenti = 'mock_utenti';
  private readonly chiavePassword = 'mock_passwords';
  private readonly chiaveUtenteLoggato = 'mock_utente_loggato';
  private readonly chiaveToken = 'auth_token';

  readonly utenteCorrente = signal<Utente | null>(
    this.leggiUtenteLoggato()
  );

  readonly token = signal<string | null>(
    localStorage.getItem(this.chiaveToken)
  );

  constructor() {
    this.inizializzaUtenti();
  }

  private inizializzaUtenti(): void {
    if (!localStorage.getItem(this.chiaveUtenti)) {
      const utentiIniziali: Utente[] = [
        {
          id: 1,
          nome: 'Mario',
          cognome: 'Rossi',
          email: 'mario@example.com',
          ruolo: 'utente',
          torneiIscrittiIds: [3]
        },
        {
          id: 2,
          nome: 'Admin',
          cognome: 'Admin',
          email: 'admin@example.com',
          ruolo: 'admin',
          torneiIscrittiIds: []
        }
      ];

      localStorage.setItem(
        this.chiaveUtenti,
        JSON.stringify(utentiIniziali)
      );
    }

    if (!localStorage.getItem(this.chiavePassword)) {
      localStorage.setItem(
        this.chiavePassword,
        JSON.stringify({
          'mario@example.com': 'demo123',
          'admin@example.com': 'admin123'
        })
      );
    }
  }

  private leggiUtenteLoggato(): Utente | null {
    try {
      const dato = localStorage.getItem(this.chiaveUtenteLoggato);
      return dato ? JSON.parse(dato) as Utente : null;
    } catch {
      return null;
    }
  }

  private leggiUtenti(): Utente[] {
    try {
      return JSON.parse(
        localStorage.getItem(this.chiaveUtenti) || '[]'
      ) as Utente[];
    } catch {
      return [];
    }
  }

  private leggiPassword(): Record<string, string> {
    try {
      return JSON.parse(
        localStorage.getItem(this.chiavePassword) || '{}'
      ) as Record<string, string>;
    } catch {
      return {};
    }
  }

  accedi(email: string, password: string): boolean {
    const emailNormalizzata = email.trim().toLowerCase();
    const utenti = this.leggiUtenti();

    const utente = utenti.find(
      u => u.email.trim().toLowerCase() === emailNormalizzata
    );

    const passwordSalvate = this.leggiPassword();

    if (
      !utente ||
      passwordSalvate[emailNormalizzata] !== password
    ) {
      return false;
    }

    const tokenFinto = `mock-token-${utente.id}-${Date.now()}`;

    this.utenteCorrente.set(utente);
    this.token.set(tokenFinto);

    localStorage.setItem(
      this.chiaveUtenteLoggato,
      JSON.stringify(utente)
    );
    localStorage.setItem(this.chiaveToken, tokenFinto);

    return true;
  }

  registra(
    nome: string,
    cognome: string,
    email: string,
    password: string
  ): boolean {
    const nomePulito = nome.trim();
    const cognomePulito = cognome.trim();
    const emailNormalizzata = email.trim().toLowerCase();

    if (
      !nomePulito ||
      !cognomePulito ||
      !emailNormalizzata ||
      !password
    ) {
      return false;
    }

    const utenti = this.leggiUtenti();

    if (
      utenti.some(
        u => u.email.trim().toLowerCase() === emailNormalizzata
      )
    ) {
      return false;
    }

    const nuovoUtente: Utente = {
      id: utenti.length > 0
        ? Math.max(...utenti.map(u => u.id)) + 1
        : 1,
      nome: nomePulito,
      cognome: cognomePulito,
      email: emailNormalizzata,
      ruolo: 'utente',
      torneiIscrittiIds: []
    };

    utenti.push(nuovoUtente);

    const passwordSalvate = this.leggiPassword();
    passwordSalvate[emailNormalizzata] = password;

    localStorage.setItem(
      this.chiaveUtenti,
      JSON.stringify(utenti)
    );

    localStorage.setItem(
      this.chiavePassword,
      JSON.stringify(passwordSalvate)
    );

    return this.accedi(emailNormalizzata, password);
  }

  aggiornaIscrizioniTornei(
    torneoId: number,
    iscrivi: boolean
  ): void {
    const utente = this.utenteCorrente();

    if (!utente) return;

    const iscrizioni = new Set(
      utente.torneiIscrittiIds ?? []
    );

    if (iscrivi) {
      iscrizioni.add(torneoId);
    } else {
      iscrizioni.delete(torneoId);
    }

    const utenteAggiornato: Utente = {
      ...utente,
      torneiIscrittiIds: [...iscrizioni]
    };

    const utentiAggiornati = this.leggiUtenti().map(u =>
      u.id === utente.id ? utenteAggiornato : u
    );

    this.utenteCorrente.set(utenteAggiornato);

    localStorage.setItem(
      this.chiaveUtenteLoggato,
      JSON.stringify(utenteAggiornato)
    );

    localStorage.setItem(
      this.chiaveUtenti,
      JSON.stringify(utentiAggiornati)
    );
  }

  esci(): void {
    this.utenteCorrente.set(null);
    this.token.set(null);

    localStorage.removeItem(this.chiaveUtenteLoggato);
    localStorage.removeItem(this.chiaveToken);
  }
}
