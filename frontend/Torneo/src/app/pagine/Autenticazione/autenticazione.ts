import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { AutenticazioneMockService } from '../../services/autenticazione.service';

@Component({
  selector: 'app-autenticazione',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './autenticazione.html',
  styleUrl: './autenticazione.css'
})
export class Autenticazione implements OnInit {
  private readonly autenticazioneService = inject(AutenticazioneMockService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  modalita: 'login' | 'registrazione' = 'login';
  messaggio: string | null = null;
  errore: string | null = null;

  nome = '';
  cognome = '';
  email = '';
  password = '';
  confermaPassword = '';

  ngOnInit(): void {
    const path = this.route.snapshot.url[0]?.path;
    if (path === 'registrazione') {
      this.modalita = 'registrazione';
    }
  }

  cambiaModalita(nuovaModalita: 'login' | 'registrazione'): void {
    this.modalita = nuovaModalita;
    this.messaggio = null;
    this.errore = null;
  }

  inviaModulo(): void {
    this.messaggio = null;
    this.errore = null;

    if (this.modalita === 'registrazione') {
      if (!this.nome || !this.email || !this.password) {
        this.errore = 'Compila tutti i campi obbligatori.';
        return;
      }

      if (this.password !== this.confermaPassword) {
        this.errore = 'Le password non coincidono.';
        return;
      }

      this.autenticazioneService.registra(
        this.nome,
        this.cognome,
        this.email,
        this.password
      ).subscribe(esito => {
        if (esito) {
          this.messaggio = 'Registrazione completata con successo!';
          setTimeout(() => this.router.navigate(['/area-utente']), 600);
        } else {
          this.errore = 'Impossibile completare la registrazione. Email già in uso o errore server.';
        }
      });

    } else {
      if (!this.email || !this.password) {
        this.errore = 'Inserisci email e password.';
        return;
      }

      this.autenticazioneService.accedi(
        this.email,
        this.password
      ).subscribe(esitoLogin => {
        if (esitoLogin) {
          const utente = this.autenticazioneService.utenteCorrente();
          this.messaggio = `Bentornato, ${utente?.nome}!`;
          const rotta = utente?.ruolo === 'admin' ? '/admin' : '/area-utente';
          setTimeout(() => this.router.navigate([rotta]), 600);
        } else {
          this.errore = 'Credenziali non valide. Controlla email e password.';
        }
      });
    }
  }
}