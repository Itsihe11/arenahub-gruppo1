export interface TorneoApi {
  id: number;
  nome: string;
  gioco: string;
  descrizione: string;
  stato: 'aperto' | 'chiuso' | 'concluso';
  dataTorneo: string;
  postiMax: number;
  iscritti: number;
  dataInizioIscrizione?: string;
  dataFineIscrizione?: string;
}

export interface Torneo extends TorneoApi {
  data: string;
}

export interface PartecipanteTorneo {
  idUtente: number;
  nome: string;
  cognome: string;
  dataIscrizione?: string;
  posizione?: number | null;
}

export interface RisultatoTorneoPayload {
  idUtente: number;
  posizione: number;
}