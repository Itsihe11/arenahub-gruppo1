export interface Utente {
  id: number;
  nome: string;
  cognome: string;
  email: string;
  ruolo: 'utente' | 'admin';
  torneiIscrittiIds?: number[];
}

export interface AuthResponse {
  token: string;
  utente: Utente;
}