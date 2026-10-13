
import { Injectable, signal, inject } from '@angular/core';
import { AutenticazioneMockService } from './autenticazione-mock.service';

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
  private readonly authService = inject(AutenticazioneMockService);
  private readonly chiaveTornei = 'mock_tornei';

  readonly tornei = signal<Torneo[]>(this.caricaTornei());

  private caricaTornei(): Torneo[] {
    try {
      const salvati = localStorage.getItem(this.chiaveTornei);

      if (salvati) {
        const tornei = JSON.parse(salvati) as Torneo[];
        return tornei.map(t => this.normalizzaTorneo(t));
      }
    } catch {
      // Se i dati salvati non sono validi, vengono inizializzati.
    }

    const iniziali: Torneo[] = [
      {
        id: 1,
        nome: 'Torneo d\'Autunno',
        gioco: 'Calcio Virtuale',
        descrizione: 'Sfida 1v1 ITS',
        dataTorneo: '2026-10-16',
        postiMax: 16,
        iscritti: 7,
        stato: 'aperto',
        partecipantiIds: [1, 101, 102, 103, 104, 105, 106],
        classificaFinale: []
      },
      {
        id: 2,
        nome: 'Sfida di Natale',
        gioco: 'Corse',
        descrizione: 'Gara finale',
        dataTorneo: '2026-12-18',
        postiMax: 12,
        iscritti: 2,
        stato: 'chiuso',
        partecipantiIds: [101, 102],
        classificaFinale: []
      }
    ];

    this.salvaTornei(iniziali);
    return iniziali;
  }

  private normalizzaTorneo(torneo: Torneo): Torneo {
    const partecipantiIds = [...new Set(torneo.partecipantiIds ?? [])];

    return {
      ...torneo,
      partecipantiIds,
      iscritti: partecipantiIds.length,
      classificaFinale: torneo.classificaFinale ?? []
    };
  }

  private salvaTornei(tornei: Torneo[]): void {
    localStorage.setItem(this.chiaveTornei, JSON.stringify(tornei));
  }

  private aggiornaTornei(aggiornamento: (tornei: Torneo[]) => Torneo[]): void {
    const aggiornati = aggiornamento(this.tornei());
    this.tornei.set(aggiornati);
    this.salvaTornei(aggiornati);
  }

  getTorneoById(id: number): Torneo | undefined {
    return this.tornei().find(t => t.id === id);
  }

  creaTorneo(
    nuovo: Omit<
      Torneo,
      'id' | 'iscritti' | 'stato' | 'partecipantiIds' | 'classificaFinale'
    >
  ): void {
    const listaCorrente = this.tornei();

    const nuovoId = listaCorrente.length > 0
      ? Math.max(...listaCorrente.map(t => t.id)) + 1
      : 1;

    const torneoCompleto: Torneo = {
      ...nuovo,
      id: nuovoId,
      iscritti: 0,
      stato: 'aperto',
      partecipantiIds: [],
      classificaFinale: []
    };

    this.aggiornaTornei(lista => [torneoCompleto, ...lista]);
  }

  cambiaStatoTorneo(
    id: number,
    nuovoStato: 'aperto' | 'chiuso' | 'concluso'
  ): void {
    this.aggiornaTornei(lista =>
      lista.map(t =>
        t.id === id ? { ...t, stato: nuovoStato } : t
      )
    );
  }

  eliminaTorneo(id: number): void {
    this.aggiornaTornei(lista =>
      lista.filter(t => t.id !== id)
    );

    // Aggiorna l'utente corrente solo se è autenticato.
    this.authService.aggiornaIscrizioniTornei(id, false);
  }

  iscriviUtente(torneoId: number, utenteId: number): boolean {
    const torneo = this.getTorneoById(torneoId);

    if (!torneo || torneo.stato !== 'aperto') return false;
    if (torneo.partecipantiIds?.includes(utenteId)) return false;
    if ((torneo.partecipantiIds?.length ?? 0) >= torneo.postiMax) {
      return false;
    }

    this.aggiornaTornei(lista =>
      lista.map(t => {
        if (t.id !== torneoId) return t;

        const partecipanti = [...(t.partecipantiIds ?? []), utenteId];

        return {
          ...t,
          partecipantiIds: partecipanti,
          iscritti: partecipanti.length
        };
      })
    );

    this.aggiornaIscrizioneUtente(utenteId, torneoId, true);
    return true;
  }

  disiscriviUtente(torneoId: number, utenteId: number): void {
    const torneo = this.getTorneoById(torneoId);

    if (!torneo?.partecipantiIds?.includes(utenteId)) return;

    this.aggiornaTornei(lista =>
      lista.map(t => {
        if (t.id !== torneoId) return t;

        const partecipanti = (t.partecipantiIds ?? [])
          .filter(id => id !== utenteId);

        return {
          ...t,
          partecipantiIds: partecipanti,
          iscritti: partecipanti.length
        };
      })
    );

    this.aggiornaIscrizioneUtente(utenteId, torneoId, false);
  }

  private aggiornaIscrizioneUtente(
    utenteId: number,
    torneoId: number,
    iscrivi: boolean
  ): void {
    const utente = this.authService.utenteCorrente();

    if (!utente || utente.id !== utenteId) return;

    this.authService.aggiornaIscrizioniTornei(torneoId, iscrivi);
  }

  salvaRisultatoGiocatore(
    torneoId: number,
    utenteId: number,
    posizione: number,
    punteggio: number
  ): void {
    if (
      !Number.isInteger(posizione) ||
      posizione < 1 ||
      !Number.isFinite(punteggio) ||
      punteggio < 0
    ) {
      return;
    }

    this.aggiornaTornei(lista =>
      lista.map(t => {
        if (t.id !== torneoId) return t;

        const classifica = [...(t.classificaFinale ?? [])];
        const indice = classifica.findIndex(
          risultato => risultato.utenteId === utenteId
        );

        const risultato = { utenteId, posizione, punteggio };

        if (indice >= 0) {
          classifica[indice] = risultato;
        } else {
          classifica.push(risultato);
        }

        return {
          ...t,
          classificaFinale: classifica.sort(
            (a, b) => a.posizione - b.posizione
          )
        };
      })
    );
  }
}
