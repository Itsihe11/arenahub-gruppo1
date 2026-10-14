import { Component, computed, inject, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { DatePipe, UpperCasePipe } from '@angular/common';

import { AutenticazioneMockService } from '../../services/autenticazione.service';
import { TorneiService } from '../../services/tornei.service';

@Component({
  selector: 'app-area-utente',
  standalone: true,
  imports: [RouterLink, DatePipe, UpperCasePipe],
  templateUrl: './area-utente.html',
  styleUrl: './area-utente.css'
})
export class AreaUtente implements OnInit {
  private readonly autenticazioneService = inject(AutenticazioneMockService);
  private readonly torneiService = inject(TorneiService);
  private readonly router = inject(Router);

  readonly utenteCorrente = this.autenticazioneService.utenteCorrente;
  readonly tuttiTornei = this.torneiService.tornei;

  ngOnInit(): void {
   
    this.torneiService.caricaTornei();
  }


  readonly mieiTornei = computed(() => {
    const utente = this.utenteCorrente();
    if (!utente) return [];

    const iscrizioniUtente = (utente.torneiIscrittiIds ?? []).map(id => Number(id));

    return this.tuttiTornei().filter(torneo => {
      const tId = Number(torneo.id);
      const iscrittoTraPartecipanti = (torneo.partecipantiIds ?? [])
        .map(id => Number(id))
        .includes(utente.id);
      const iscrittoNelProfilo = iscrizioniUtente.includes(tId);

      return iscrittoTraPartecipanti || iscrittoNelProfilo;
    });
  });


  readonly torneiAttiviCount = computed(() =>
    this.mieiTornei().filter(torneo => torneo.stato === 'aperto').length
  );

  readonly torneiCompletatiCount = computed(() =>
    this.mieiTornei().filter(
      torneo => torneo.stato === 'chiuso'
    ).length
  );

  annullaIscrizione(torneoId: number): void {
    const utente = this.utenteCorrente();

    if (!utente) {
      this.router.navigate(['/login']);
      return;
    }

    if (
      confirm(
        'Sei sicuro di voler annullare la tua iscrizione a questo torneo?'
      )
    ) {
      
      this.autenticazioneService
        .aggiornaIscrizioniTornei(torneoId, false)
        .subscribe({
          next: (successo) => {
            if (successo) {
            
              alert('Iscrizione annullata con successo.');
            } else {
              alert('Si è verificato un errore durante l\'annullamento.');
            }
          },
          error: (err) => {
            console.error('Errore durante l\'annullamento:', err);
            alert('Errore di connessione al server.');
          }
        });
    }
  }

  esci(): void {
    this.autenticazioneService.esci();
    this.router.navigate(['/login']);
  }
}