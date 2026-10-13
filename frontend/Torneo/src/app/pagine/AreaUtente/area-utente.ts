
import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { DatePipe, UpperCasePipe } from '@angular/common';

import { AutenticazioneMockService } from '../../services/autenticazione-mock.service';
import { TorneiService } from '../../services/tornei.service';

@Component({
  selector: 'app-area-utente',
  standalone: true,
  imports: [RouterLink, DatePipe, UpperCasePipe],
  templateUrl: './area-utente.html',
  styleUrl: './area-utente.css'
})
export class AreaUtente {
  private readonly autenticazioneService = inject(AutenticazioneMockService);
  private readonly torneiService = inject(TorneiService);
  private readonly router = inject(Router);

  readonly utenteCorrente = this.autenticazioneService.utenteCorrente;
  readonly tuttiTornei = this.torneiService.tornei;

  // Tornei a cui l'utente risulta iscritto.
  readonly mieiTornei = computed(() => {
    const utente = this.utenteCorrente();

    if (!utente) return [];

    const iscrizioniUtente = utente.torneiIscrittiIds ?? [];

    return this.tuttiTornei().filter(torneo =>
      (torneo.partecipantiIds ?? []).includes(utente.id) ||
      iscrizioniUtente.includes(torneo.id)
    );
  });

  // Numero di tornei con iscrizioni aperte.
  readonly torneiAttiviCount = computed(() =>
    this.mieiTornei().filter(torneo =>
      torneo.stato === 'aperto'
    ).length
  );

  // Numero di tornei chiusi o conclusi.
  readonly torneiCompletatiCount = computed(() =>
    this.mieiTornei().filter(torneo =>
      torneo.stato === 'concluso' || torneo.stato === 'chiuso'
    ).length
  );

  annullaIscrizione(torneoId: number): void {
    const utente = this.utenteCorrente();

    if (!utente) {
      this.router.navigate(['/login']);
      return;
    }

    const torneo = this.torneiService.getTorneoById(torneoId);

    if (!torneo) {
      alert('Torneo non trovato.');
      return;
    }

    const iscrittoTraPartecipanti =
      torneo.partecipantiIds?.includes(utente.id) ?? false;

    const iscrittoNelProfilo =
      utente.torneiIscrittiIds?.includes(torneoId) ?? false;

    if (!iscrittoTraPartecipanti && !iscrittoNelProfilo) {
      alert('Non risulti iscritto a questo torneo.');
      return;
    }

    if (
      confirm(
        'Sei sicuro di voler annullare la tua iscrizione a questo torneo?'
      )
    ) {
      this.torneiService.disiscriviUtente(torneoId, utente.id);

      alert('Iscrizione annullata con successo.');
    }
  }

  esci(): void {
    this.autenticazioneService.esci();
    this.router.navigate(['/login']);
  }
}
