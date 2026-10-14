ENDPOINTS


## GET /api/utenti
Lista di tutti gli utenti registrati. Accesso: admin.

| Parametro     | Dove   | Tipo    | Obbligatorio |
|---------------|--------|---------|--------------|
| Authorization | header | stringa | si           |

**Risposta 200**
[ { "id": 1, "nome": "mario", “cognome”:”rossi”,"email": "mario@example.com", "ruolo": "utente" },
  { "id": 2, "nome": "admin", “cognome”:”admin”, "email": "admin@example.com", "ruolo": "admin" } ]

**Errori**
| 401 | token mancante o non valido | {"errore": "Non autenticato"} |
| 403 | utente senza ruolo admin    | {"errore": "Accesso negato"} |

—------------------------------------------------------------------------------------------------------------------------

## GET /api/tornei
Lista dei tornei. Accesso: pubblico.

| Parametro | Dove  | Tipo    | Obbligatorio |
|-----------|-------|---------|--------------|
| stato     | query | stringa | no           |
| data      | query | date     | no           |
| gioco    | query | stringa | no           |

**Risposta 200**
[ { "id": 3, "nome": "Torneo d'autunno", "gioco": "Calcio virtuale", "descrizione": "...", "stato": "aperto",
    "dataTorneo": "2026-10-16T15:00:00", "postiMax": 16, "iscritti": 7 } ]

**Errori**
| 400 | stato non valido (ammessi: aperto, chiuso) | {"errore": "Stato non valido"} |

—------------------------------------------------------------------------------------------------------------------------

## GET /api/tornei/{id}
Dettaglio di un torneo. Accesso: pubblico.

| Parametro | Dove | Tipo   | Obbligatorio |
|-----------|------|--------|--------------|
| id        | path | intero | si           |

**Risposta 200**
{ "id": 3, "nome": "Torneo d'autunno", "gioco": "Calcio virtuale", "descrizione": "...",  "stato": "aperto",
  "dataTorneo": "2026-10-16T15:00:00", "postiMax": 16, "iscritti": 8 }

**Errori**
| 400 | id non numerico | {"errore": "Id non valido"} |
| 404 | id inesistente  | {"errore": "Torneo non trovato"} |

—------------------------------------------------------------------------------------------------------------------------

## POST /api/tornei
Crea un nuovo torneo. Accesso: admin.

| Parametro     | Dove   | Tipo    | Obbligatorio |
|---------------|--------|---------|--------------|
| Authorization | header | stringa | si           |
| nome          | body   | stringa | si           |
| gioco         | body   | stringa | si           |
| descrizione   | body   | stringa | si           |
| dataTorneo      | body   | data    | si           |
| dataInizioIscrizione       | body   | data    | si           |
| dataFineIscrizione       | body   | data    | si           |
| postiMax      | body   | intero  | si           |

**Risposta 201**
{ "id": 4, "nome": "Torneo d'inverno", "gioco": "Calcio virtuale", "descrizione": "...", "stato": "aperto",
  "dataTorneo": "2026-12-20T18:00:00", "postiMax": 32, "iscritti": 0, “dataInizioIscrizione”:”2026-12-10”,”dataFineIscrizione”: “2026-12-19”}

**Errori**
| 400 | campo mancante o non valido (es. postiMax <= 0, dataTorneo nel passato) | {"errore": "Dati non validi"} |
| 401 | token mancante o non valido | {"errore": "Non autenticato"} |
| 403 | utente senza ruolo admin    | {"errore": "Accesso negato"} |

—------------------------------------------------------------------------------------------------------------------------

## DELETE /api/tornei/{id}
Cancella un torneo. Accesso: admin.

| Parametro     | Dove   | Tipo    | Obbligatorio |
|---------------|--------|---------|--------------|
| Authorization | header | stringa | si           |
| id            | path   | intero  | si           |

**Risposta 204**
(nessun contenuto)

**Errori**
| 401 | token mancante o non valido | {"errore": "Non autenticato"} |
| 403 | utente senza ruolo admin    | {"errore": "Accesso negato"} |
| 404 | id inesistente              | {"errore": "Torneo non trovato"} |

—------------------------------------------------------------------------------------------------------------------------

## POST /api/login
Autentica un utente e restituisce il token. Accesso: pubblico.

| Parametro | Dove | Tipo    | Obbligatorio |
|-----------|------|---------|--------------|
| email    | body | stringa | si           |
| password  | body | stringa | si           |

**Risposta 200**
{ "token": "eyJhbGciOi...", "utente": { "id": 1, "nome": "mario", "cognome": "rossi", "ruolo": "utente" } }

**Errori**
| 400 | email o password mancanti | {"errore": "Dati mancanti"} |
| 401 | credenziali errate           | {"errore": "Credenziali non valide"} |

—------------------------------------------------------------------------------------------------------------------------

## POST /api/registrazione
Crea un nuovo account con ruolo utente. Accesso: pubblico.

| Parametro | Dove | Tipo    | Obbligatorio |
|-----------|------|---------|--------------|
| nome      | body | stringa | si           |
| cognome   | body | stringa | si           |
| email     | body | stringa | si           |
| password  | body | stringa | si           |

**Risposta 201**
{ "id": 5, "nome": "luigi", "cognome": "bianchi", "email": "luigi@example.com", "ruolo": "utente" }

**Errori**
| 400 | campo mancante, email non valida o password troppo corta | {"errore": "Dati non validi"} |
| 409 | email già in uso     		                         | {"errore": "Utente già esistente"} |

—------------------------------------------------------------------------------------------------------------------------

## POST /api/tornei/{id}/iscrizione
Iscrive l'utente autenticato a un torneo. Accesso: utente autenticato.

| Parametro     | Dove   | Tipo    | Obbligatorio |
|---------------|--------|---------|--------------|
| Authorization | header | stringa | si           |
| id            | path   | intero  | si           |

**Risposta 201**
{ "idTorneo": 3, "idUtente": 1, "dataIscrizione": "2026-10-12T12:30:00",
  "torneo": { "id": 3, "nome": "Torneo d'autunno", "iscritti": 8, "postiMax": 16 } }

**Errori**
| 401 | token mancante o non valido                | {"errore": "Non autenticato"} |
| 404 | id inesistente                             | {"errore": "Torneo non trovato"} |
| 409 | utente già iscritto a questo torneo        | {"errore": "Già iscritto"} |
| 409 | torneo con stato chiuso                    | {"errore": "Iscrizioni chiuse"} |
| 409 | posti esauriti (iscritti = postiMax)       | {"errore": "Posti esauriti"} |

—------------------------------------------------------------------------------------------------------------------------

## GET /api/utenti/me/iscrizioni
Lista dei tornei a cui l'utente autenticato è iscritto. Accesso: utente autenticato.

| Parametro     | Dove   | Tipo    | Obbligatorio |
|---------------|--------|---------|--------------|
| Authorization | header | stringa | si           |

**Risposta 200**
[ { "id": 3, "nome": "Torneo d'autunno", "gioco": "Calcio virtuale", "descrizione": "...", "stato": "aperto",
    "dataTorneo": "2026-10-16T15:00:00", "postiMax": 16, "iscritti": 8,
    "dataIscrizione": "2026-10-12T12:30:00" } ]

**Errori**
| 401 | token mancante o non valido | {"errore": "Non autenticato"} |

—------------------------------------------------------------------------------------------------------------------------


## GET /api/utenti/me
Dati personali dell'utente autenticato. Accesso: utente autenticato.

| Parametro     | Dove   | Tipo    | Obbligatorio |
|---------------|--------|---------|--------------|
| Authorization | header | stringa | si           |

**Risposta 200**
{ "id": 1, "nome": "Mario", "cognome": "Rossi",
  "email": "mario@example.com", "ruolo": "utente" }

**Errori**
| 401 | token mancante o non valido | {"errore": "Non autenticato"} |

—------------------------------------------------------------------------------------------------------------------------

## GET /api/utenti/me/risultati
Risultati dei tornei a cui l'utente autenticato ha partecipato. Accesso: utente autenticato.

| Parametro     | Dove   | Tipo    | Obbligatorio |
|---------------|--------|---------|--------------|
| Authorization | header | stringa | si           |

**Risposta 200**
[ { "idTorneo": 3, "nome": "Torneo d'autunno", "gioco": "Calcio virtuale",
    "stato": "concluso", "dataTorneo": "2026-10-16T15:00:00",
    "posizione": 2, "partecipanti": 16 },
  { "idTorneo": 5, "nome": "Torneo d'inverno", "gioco": "Calcio virtuale",
    "stato": "aperto", "dataTorneo": "2026-12-20T18:00:00",
    "posizione": null, "partecipanti": 9 } ]

**Errori**
| 401 | token mancante o non valido | {"errore": "Non autenticato"} |

—------------------------------------------------------------------------------------------------------------------------

## POST /api/tornei/{id}/risultati
Inserisce i risultati dei partecipanti di un torneo e lo porta allo stato concluso. Accesso: admin.

| Parametro              | Dove   | Tipo    | Obbligatorio |
|------------------------|--------|---------|--------------|
| Authorization          | header | stringa | si           |
| id                     | path   | intero  | si           |
| risultati              | body   | array   | si           |
| risultati[].idUtente   | body   | intero  | si           |
| risultati[].posizione  | body   | intero  | si           |

**Risposta 201**
{ "idTorneo": 3, "nome": "Torneo d'autunno", "stato": "concluso",
  "risultati": [ { "idUtente": 1, "nome": "mario", "cognome": "rossi", "posizione": 1 },
                 { "idUtente": 4, "nome": "luigi", "cognome": "bianchi", "posizione": 2 } ] }

**Errori**
| 400 | id non numerico, risultati vuoto o malformato, posizione <= 0 o duplicata | {"errore": "Dati non validi"} |
| 400 | idUtente non iscritto al torneo                                           | {"errore": "Utente non iscritto"} |
| 401 | token mancante o non valido                                               | {"errore": "Non autenticato"} |
| 403 | utente senza ruolo admin                                                  | {"errore": "Accesso negato"} |
| 404 | id torneo inesistente                                                     | {"errore": "Torneo non trovato"} |
| 409 | risultati già inseriti per questo torneo                                  | {"errore": "Risultati già presenti"} |

—------------------------------------------------------------------------------------------------------------------------
## PUT /api/tornei/{id}/risultati
Modifica i risultati dei partecipanti di un torneo e lo porta allo stato concluso. Accesso: admin.

| Parametro              | Dove   | Tipo    | Obbligatorio |
|------------------------|--------|---------|--------------|
| Authorization          | header | stringa | si           |
| id                     | path   | intero  | si           |
| risultati              | body   | array   | si           |
| risultati[].idUtente   | body   | intero  | si           |
| risultati[].posizione  | body   | intero  | si           |

**Risposta 201**
{ "idTorneo": 3, "nome": "Torneo d'autunno", "stato": "concluso",
  "risultati": [ { "idUtente": 1, "nome": "mario", "cognome": "rossi", "posizione": 1 },
                 { "idUtente": 4, "nome": "luigi", "cognome": "bianchi", "posizione": 2 } ] }

**Errori**
| 400 | id non numerico, risultati vuoto o malformato, posizione <= 0 o duplicata | {"errore": "Dati non validi"} |
| 400 | idUtente non iscritto al torneo                                           | {"errore": "Utente non iscritto"} |
| 401 | token mancante o non valido                                               | {"errore": "Non autenticato"} |
| 403 | utente senza ruolo admin                                                  | {"errore": "Accesso negato"} |
| 404 | id torneo inesistente                                                     | {"errore": "Torneo non trovato"} |
| 409 | risultati già inseriti per questo torneo                                  | {"errore": "Risultati già presenti"} |

—------------------------------------------------------------------------------------------------------------------------




## DELETE /api/tornei/{id}/iscrizione
Annulla l'iscrizione dell'utente autenticato a un torneo. Accesso: utente autenticato.

| Parametro     | Dove   | Tipo    | Obbligatorio |
|---------------|--------|---------|--------------|
| Authorization | header | stringa | si           |
| id            | path   | intero  | si           |

**Risposta 200**
{ "idTorneo": 3, "idUtente": 1,
  "torneo": { "id": 3, "nome": "Torneo d'autunno", "iscritti": 7, "postiMax": 16 } }

**Errori**
| 400 | id non numerico                                          | {"errore": "Id non valido"} |
| 401 | token mancante o non valido                              | {"errore": "Non autenticato"} |
| 404 | id torneo inesistente                                    | {"errore": "Torneo non trovato"} |
| 404 | utente non iscritto a questo torneo                      | {"errore": "Iscrizione non trovata"} |
| 409 | torneo concluso o già iniziato (annullamento non ammesso) | {"errore": "Annullamento non consentito"} |

—------------------------------------------------------------------------------------------------------------------------

## GET /api/tornei/{id}/partecipanti
Lista degli utenti iscritti a un torneo. Accesso: pubblico.

| Parametro | Dove | Tipo   | Obbligatorio |
|-----------|------|--------|--------------|
| id        | path | intero | si           |

**Risposta 200**
{ "idTorneo": 3, "nome": "Torneo d'autunno", "iscritti": 2, "postiMax": 16,
  "partecipanti": [ { "idUtente": 1, "nome": "mario", “cognome”: “rossi”, "dataIscrizione": "2026-10-12T12:30:00" },
                    { "idUtente": 4, "nome": "luigi", “cognome”: “bianchi”,  "dataIscrizione": "2026-10-13T09:10:00" } ] }

**Errori**
| 400 | id non numerico       | {"errore": "Id non valido"} |
| 404 | id torneo inesistente | {"errore": "Torneo non trovato"} |

—------------------------------------------------------------------------------------------------------------------------

## PUT /api/tornei/{id}
Modifica un torneo esistente. Accesso: admin.

| Parametro     | Dove   | Tipo    | Obbligatorio |
|---------------|--------|---------|--------------|
| Authorization | header | stringa | si         |
| id                 | path   | intero  | si              |
| nome            | body   | stringa | no           |
| gioco            | body   | stringa | no           |
| dataTorneo   | body   | data    | no           |
| dataInizioIscrizione   | body   | data    | no           |
| dataFineIscrizione   | body   | data    | no           |
| postiMax      | body   | intero  | no           |
| stato            | body   | stringa | no           |

**Risposta 200**
{ "id": 3, "nome": "Torneo d'autunno 2026", "gioco": "Calcio virtuale", "stato": "aperto",
  "dataTorneo": "2026-10-18T15:00:00", "postiMax": 24, "iscritti": 8 }

**Errori**
| 400 | id non numerico, campo mancante o non valido (es. postiMax <= 0, stato non ammesso) | {"errore": "Dati non validi"} |
| 401 | token mancante o non valido                                                         | {"errore": "Non autenticato"} |
| 403 | utente senza ruolo admin                                                            | {"errore": "Accesso negato"} |
| 404 | id torneo inesistente                                                               | {"errore": "Torneo non trovato"} |
| 409 | postiMax inferiore al numero di iscritti attuali                                    | {"errore": "Posti insufficienti"} |
| 409 | torneo concluso (modifica non ammessa)                                              | {"errore": "Torneo concluso"} |


—------------------------------------------------------------------------------------------------------------------------


CHANGE REQUEST
## GET /api/admin/maxTornei
Numero massimo di iscrizioni per gli utenti. Accesso: admin.

| Parametro | Dove | Tipo   | Obbligatorio |
|-----------|------|--------|--------------|
| Authorization | header | stringa | si|
| id         | path | intero | si |

**Risposta 200**
{ “maxTorneiAttivi”: 3 }

**Errori**
| 401 | token mancante o non valido | {"errore": "Non autenticato"} |
| 403 | utente senza ruolo admin    | {"errore": "Accesso negato"} |

—------------------------------------------------------------------------------------------------------------------------


## GET /api/utente/me/statoIscrizione

Numero massimo di iscrizioni per il singolo utente. Accesso: utente autenticato.

| Parametro | Dove | Tipo   | Obbligatorio |
|-----------|------|--------|--------------|
| Authorization | header | stringa | si|

**Risposta 200**
{ “torneiAttiviConteggiati”: 3, “limiteMassimo”: 3, “canRegister”: false }

**Errori**
| 401 | token mancante o non valido | {"errore": "Non autenticato"} |
| 404 | id utente inesistente | {"errore": "Utente non trovato"} |


—------------------------------------------------------------------------------------------------------------------------






## PUT /api/admin/maxTornei
Modifica numero massimo di iscrizioni per gli utenti. Accesso: admin.

| Parametro | Dove | Tipo   | Obbligatorio |
|-----------|------|--------|--------------|
| Authorization | header | stringa | si |
| id         | path | intero | si |
| maxTorneiAttivi | path | intero | si |

**Risposta 200**
{ “maxTorneiAttivi”: 3 }

**Errori**
| 400 | numero mancante o non valido | {"errore": "Numero mancante o non valido"} |
| 401 | token mancante o non valido | {"errore": "Non autenticato"} |
| 403 | utente senza ruolo admin    | {"errore": "Accesso negato"} |

—------------------------------------------------------------------------------------------------------------------------

INTEGRAZIONE TABELLA PARTITE



## GET /api/partite/torneo/{torneoId}
Lista delle partite di un torneo (tabellone). Accesso: pubblico.

| Parametro | Dove | Tipo   | Obbligatorio |
|-----------|------|--------|--------------|
| torneoId  | path | intero | si           |

**Risposta 200**
[ { "id": 1, "torneoId": 3, "turno": 1,
    "giocatore1": { "id": 1, "nome": "mario", "cognome": "rossi" },
    "giocatore2": { "id": 4, "nome": "luigi", "cognome": "bianchi" },
    "punteggio1": 2, "punteggio2": 1, "stato": "completata" },
  { "id": 2, "torneoId": 3, "turno": 1,
    "giocatore1": { "id": 5, "nome": "anna", "cognome": "verdi" },
    "giocatore2": { "id": 6, "nome": "paolo", "cognome": "neri" },
    "punteggio1": null, "punteggio2": null, "stato": "da_giocare" } ]


**Errori**
| 400 | torneoId non numerico | {"errore": "Id non valido"} |
| 404 | torneo inesistente    | {"errore": "Torneo non trovato"} |

—------------------------------------------------------------------------------------------------------------------------

## POST /api/partite/genera/{torneoId}
Genera il tabellone (accoppiamenti) di un torneo. Accesso: admin.

| Parametro     | Dove   | Tipo    | Obbligatorio |
|---------------|--------|---------|--------------|
| Authorization | header | stringa | si           |
| torneoId      | path   | intero  | si           |

**Risposta 201**
[ { "id": 1, "torneoId": 3, "turno": 1,
    "giocatore1": { "id": 1, "nome": "mario", "cognome": "rossi" },
    "giocatore2": { "id": 4, "nome": "luigi", "cognome": "bianchi" },
    "punteggio1": null, "punteggio2": null, "stato": "da_giocare" } ]

**Errori**
| 400 | torneoId non numerico                          | {"errore": "Id non valido"} |
| 401 | token mancante o non valido                    | {"errore": "Non autenticato"} |
| 403 | utente senza ruolo admin                       | {"errore": "Accesso negato"} |
| 404 | torneo inesistente                             | {"errore": "Torneo non trovato"} |
| 409 | tabellone già generato per questo torneo       | {"errore": "Tabellone già generato"} |
| 409 | iscritti insufficienti (meno di 2)             | {"errore": "Iscritti insufficienti"} |
| 409 | torneo concluso                                | {"errore": "Torneo concluso"} |

—------------------------------------------------------------------------------------------------------------------------

## PUT /api/partite/{id}/risultato
Salva il risultato di una singola partita. Accesso: admin.

| Parametro     | Dove   | Tipo    | Obbligatorio |
|---------------|--------|---------|--------------|
| Authorization | header | stringa | si           |
| id            | path   | intero  | si           |
| punteggio1    | body   | intero  | si           |
| punteggio2    | body   | intero  | si           |
| stato         | body   | stringa | no (default: completata) |

Stati ammessi: da_giocare, completata.

**Risposta 200**
{ "id": 1, "torneoId": 3, "turno": 1,
  "utente1": { "id": 1, "nome": "mario", "cognome": "rossi" },
  "utente2": { "id": 4, "nome": "luigi", "cognome": "bianchi" },
  "punteggio1": 2, "punteggio2": 1, "stato": "completata" }

**Errori**
| 400 | id non numerico, punteggio mancante, non numerico o negativo, stato non ammesso | {"errore": "Dati non validi"} |
| 401 | token mancante o non valido                                                     | {"errore": "Non autenticato"} |
| 403 | utente senza ruolo admin                                                        | {"errore": "Accesso negato"} |
| 404 | id partita inesistente                                                          | {"errore": "Partita non trovata"} |
| 409 | torneo concluso (modifica non ammessa)                                          | {"errore": "Torneo concluso"} |

—------------------------------------------------------------------------------------------------------------------------

## DELETE /api/partite/reset/{torneoId}
Elimina tutte le partite del torneo e riporta il tabellone allo stato iniziale. Accesso: admin.

| Parametro     | Dove   | Tipo    | Obbligatorio |
|---------------|--------|---------|--------------|
| Authorization | header | stringa | si           |
| torneoId      | path   | intero  | si           |

**Risposta 200**

**Errori**
| 400 | torneoId non numerico       | {"errore": "Id non valido"} |
| 401 | token mancante o non valido | {"errore": "Non autenticato"} |
| 403 | utente senza ruolo admin    | {"errore": "Accesso negato"} |
| 404 | torneo inesistente          | {"errore": "Torneo non trovato"} |
| 409 | torneo concluso (reset non ammesso) | {"errore": "Torneo concluso"} |

