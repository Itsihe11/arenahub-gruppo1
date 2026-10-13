export interface GiocatoreInfo {
  id: number;
  nome: string;
  cognome: string;
}

export interface Partita {
  id: number;
  torneoId: number;
  turno: number;
  dataora?: string;
  punteggio1?: number;
  punteggio2?: number;
  stato: 'da_giocare' | 'in_corso' | 'completata';
  giocatore1: GiocatoreInfo;
  giocatore2: GiocatoreInfo;
  vincitoreId?: number | null;
}