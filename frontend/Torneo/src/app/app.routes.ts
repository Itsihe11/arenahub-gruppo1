import { Routes } from '@angular/router';
import { Home } from './pagine/Home/home';
import { Tornei } from './pagine/Tornei/tornei';
import { DettaglioTorneo } from './pagine/DettaglioTorneo/dettaglio-torneo';
import { Autenticazione } from './pagine/Autenticazione/autenticazione';
import { AreaUtente } from './pagine/AreaUtente/area-utente';
import { AreaAdmin } from './pagine/AreaAdmin/area-admin';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'tornei', component: Tornei },
  { path: 'tornei/:id', component: DettaglioTorneo },
  { path: 'login', component: Autenticazione },
  { path: 'registrazione', component: Autenticazione },
  { path: 'area-utente', component: AreaUtente },
  { path: 'admin', component: AreaAdmin }
];