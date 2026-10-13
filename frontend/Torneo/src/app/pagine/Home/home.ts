import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AutenticazioneMockService } from '../../services/autenticazione.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home {
  private readonly autenticazioneService = inject(AutenticazioneMockService);
  private readonly router = inject(Router);

  readonly utenteCorrente = this.autenticazioneService.utenteCorrente;

  esci(): void {
    this.autenticazioneService.esci();
    this.router.navigate(['/login']);
  }
}