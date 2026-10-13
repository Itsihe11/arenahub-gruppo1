import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe, UpperCasePipe } from '@angular/common';

import { AutenticazioneMockService } from '../../services/autenticazione-mock.service';
import { TorneiService } from '../../services/tornei.service';

@Component({
  selector: 'app-dettaglio-torneo',
  standalone: true,
  imports: [RouterLink, DatePipe, UpperCasePipe],
  templateUrl: './dettaglio-torneo.html',
  styleUrl: './dettaglio-torneo.css'
})
export class DettaglioTorneo {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly autenticazioneService = inject(AutenticazioneMockService);
  private readonly torneiService = inject(TorneiService);

  readonly utenteCorrente = this.autenticazioneService.utenteCorrente;
  readonly torneoId = signal<number | null>(null);

  readonly torneo = computed(() => {
    const id = this.torneoId();

    if (id === null || !Number.isInteger(id) || id <= 0) {
      return undefined;
    }

    return this.torneiService.tornei().find(t => t.id === id);
  });

  readonly giaIscritto = computed(() => {
    const torneo = this.torneo();
    const utente = this.utenteCorrente();

    if (!torneo || !utente) {
      return false;
    }

    const iscrittoTraPartecipanti =
      torneo.partecipantiIds?.includes(utente.id) ?? false;

    const iscrittoNelProfilo =
      utente.torneiIscrittiIds?.includes(torneo.id) ?? false;

    return iscrittoTraPartecipanti || iscrittoNelProfilo;
  });

  constructor() {
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      const id = Number(idParam);

      this.torneoId.set(
        idParam !== null && Number.isInteger(id) && id > 0
          ? id
          : null
      );
    });
  }

  iscriviti(): void {
    const utente = this.utenteCorrente();
    const torneo = this.torneo();

    if (!utente) {
      this.router.navigate(['/login']);
      return;
    }

    if (!torneo) {
      alert('Torneo non trovato.');
      return;
    }

    if (this.giaIscritto()) {
      alert('Sei già iscritto a questo torneo.');
      return;
    }

    if (torneo.stato !== 'aperto') {
      alert('Le iscrizioni per questo torneo sono chiuse.');
      return;
    }

    if (torneo.iscritti >= torneo.postiMax) {
      alert('Il torneo ha raggiunto il numero massimo di partecipanti.');
      return;
    }

    const successo = this.torneiService.iscriviUtente(
      torneo.id,
      utente.id
    );

    if (successo) {
      alert('Iscrizione completata con successo!');
    } else {
      alert('Impossibile iscriversi. Controlla lo stato e i posti disponibili.');
    }
  }

  disiscriviti(): void {
    const utente = this.utenteCorrente();
    const torneo = this.torneo();

    if (!utente || !torneo) {
      return;
    }

    if (!this.giaIscritto()) {
      alert('Non risulti iscritto a questo torneo.');
      return;
    }

    if (torneo.stato !== 'aperto') {
      alert('Non puoi annullare l\'iscrizione: il torneo non è più aperto.');
      return;
    }

    if (
      confirm('Sei sicuro di voler annullare la tua iscrizione?')
    ) {
      this.torneiService.disiscriviUtente(
        torneo.id,
        utente.id
      );

      alert('Iscrizione annullata.');
    }
  }

  esci(): void {
    this.autenticazioneService.esci();
    this.router.navigate(['/login']);
  }
}