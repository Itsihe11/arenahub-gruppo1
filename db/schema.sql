-- 1. Tabella UTENTE
CREATE TABLE utente (
  id INT PRIMARY KEY AUTO_INCREMENT,
  nome VARCHAR(100) NOT NULL,
  cognome VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  ruolo VARCHAR(50) NOT NULL DEFAULT 'utente' CHECK (ruolo IN ('utente', 'admin'))
);

-- 2. Tabella TORNEO
CREATE TABLE torneo (
  id INT PRIMARY KEY AUTO_INCREMENT,
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

-- 3. Tabella ISCRIZIONE
CREATE TABLE iscrizione (
  id INT PRIMARY KEY AUTO_INCREMENT,
  utente_id INT NOT NULL,
  torneo_id INT NOT NULL,
  data_iscrizione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  stato VARCHAR(50) NOT NULL DEFAULT 'confermata' CHECK (stato IN ('confermata', 'in_attesa', 'annullata')),
  UNIQUE (utente_id, torneo_id),
  FOREIGN KEY (utente_id) REFERENCES utente(id) ON DELETE CASCADE,
  FOREIGN KEY (torneo_id) REFERENCES torneo(id) ON DELETE CASCADE
);

-- 4. Tabella PARTITA
CREATE TABLE partita (
  id INT PRIMARY KEY AUTO_INCREMENT,
  torneo_id INT NOT NULL,
  giocatore1 INT NOT NULL,
  giocatore2 INT NOT NULL,
  turno INT NOT NULL DEFAULT 1,
  dataora DATETIME,
  punteggio1 INT DEFAULT NULL,
  punteggio2 INT DEFAULT NULL,
  stato VARCHAR(50) NOT NULL DEFAULT 'da_giocare' CHECK (stato IN ('da_giocare', 'in_corso', 'completata')),
  vincitore_id INT DEFAULT NULL,
  FOREIGN KEY (torneo_id) REFERENCES torneo(id) ON DELETE CASCADE,
  FOREIGN KEY (giocatore1) REFERENCES utente(id) ON DELETE CASCADE,
  FOREIGN KEY (giocatore2) REFERENCES utente(id) ON DELETE CASCADE,
  FOREIGN KEY (vincitore_id) REFERENCES utente(id) ON DELETE SET NULL,
  CHECK (giocatore1 <> giocatore2)
);

-- Indici di prestazione per le query frequenti
CREATE INDEX idx_torneo_stato ON torneo(stato);
CREATE INDEX idx_iscrizione_utente ON iscrizione(utente_id);
CREATE INDEX idx_iscrizione_torneo ON iscrizione(torneo_id);
CREATE INDEX idx_partita_torneo ON partita(torneo_id);