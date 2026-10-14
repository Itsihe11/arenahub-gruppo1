CREATE TABLE utente (
  id SERIAL PRIMARY KEY,
  nome VARCHAR(100) NOT NULL,
  cognome VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  ruolo VARCHAR(50) NOT NULL DEFAULT 'utente' CHECK (ruolo IN ('utente', 'admin'))
);


CREATE TABLE torneo (
  id SERIAL PRIMARY KEY,
  nome VARCHAR(150) NOT NULL,
  gioco VARCHAR(100) NOT NULL,
  descrizione TEXT,
  data_inizio_iscrizioni DATE NOT NULL,
  data_fine_iscrizioni DATE NOT NULL,
  max_partecipanti INT CHECK (max_partecipanti IS NULL OR max_partecipanti > 0),
  data_inizio_torneo DATE NOT NULL,
  stato VARCHAR(50) NOT NULL DEFAULT 'aperto' CHECK (stato IN ('aperto', 'in corso', 'chiuso', 'cancellato')),
  CONSTRAINT check_date_iscrizioni CHECK (data_fine_iscrizioni >= data_inizio_iscrizioni),
  CONSTRAINT check_inizio_torneo CHECK (data_inizio_torneo >= data_fine_iscrizioni)
);


CREATE TABLE iscrizione (
  id SERIAL PRIMARY KEY,
  utente_id INT NOT NULL,
  torneo_id INT NOT NULL,
  data_iscrizione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  stato VARCHAR(50) NOT NULL DEFAULT 'confermata' CHECK (stato IN ('confermata', 'in_attesa', 'annullata')),
  CONSTRAINT unique_utente_torneo UNIQUE (utente_id, torneo_id),
  CONSTRAINT fk_iscrizione_utente FOREIGN KEY (utente_id) REFERENCES utente(id) ON DELETE CASCADE,
  CONSTRAINT fk_iscrizione_torneo FOREIGN KEY (torneo_id) REFERENCES torneo(id) ON DELETE CASCADE
);


CREATE TABLE partita (
  id SERIAL PRIMARY KEY,
  torneo_id INT NOT NULL,
  giocatore1 INT NOT NULL,
  giocatore2 INT NOT NULL,
  turno INT NOT NULL DEFAULT 1,
  dataora TIMESTAMP,
  punteggio1 INT DEFAULT NULL,
  punteggio2 INT DEFAULT NULL,
  stato VARCHAR(50) NOT NULL DEFAULT 'da_giocare' CHECK (stato IN ('da_giocare', 'in_corso', 'completata')),
  vincitore_id INT DEFAULT NULL,
  CONSTRAINT fk_partita_torneo FOREIGN KEY (torneo_id) REFERENCES torneo(id) ON DELETE CASCADE,
  CONSTRAINT fk_partita_giocatore1 FOREIGN KEY (giocatore1) REFERENCES utente(id) ON DELETE CASCADE,
  CONSTRAINT fk_partita_giocatore2 FOREIGN KEY (giocatore2) REFERENCES utente(id) ON DELETE CASCADE,
  CONSTRAINT fk_partita_vincitore FOREIGN KEY (vincitore_id) REFERENCES utente(id) ON DELETE SET NULL,
  CONSTRAINT check_giocatori_diversi CHECK (giocatore1 <> giocatore2)
);

ALTER TABLE utente 
ADD COLUMN max_tornei_attivi INTEGER NOT NULL DEFAULT 3;