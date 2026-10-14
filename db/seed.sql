INSERT INTO utente (nome, cognome, email, password, ruolo) VALUES
('Marco', 'Rossi', 'admin.marco@its.it', 'admin123', 'admin'),
('Laura', 'Bianchi', 'admin.laura@its.it', 'admin123', 'admin'),
('Giuseppe', 'Verdi', 'giuseppe.verdi@email.com', 'password123', 'utente'),
('Andrea', 'Ferrari', 'andrea.ferrari@email.com', 'password123', 'utente'),
('Francesca', 'Russo', 'francesca.russo@email.com', 'password123', 'utente'),
('Alessandro', 'Esposito', 'alessandro.esposito@email.com', 'password123', 'utente'),
('Sofia', 'Romano', 'sofia.romano@email.com', 'password123', 'utente'),
('Matteo', 'Gallo', 'matteo.gallo@email.com', 'password123', 'utente'),
('Chiara', 'Costa', 'chiara.costa@email.com', 'password123', 'utente'),
('Lorenzo', 'Fontana', 'lorenzo.fontana@email.com', 'password123', 'utente'),
('Elena', 'Conti', 'elena.conti@email.com', 'password123', 'utente'),
('Davide', 'Greco', 'davide.greco@email.com', 'password123', 'utente'),
('Sara', 'Lombardi', 'sara.lombardi@email.com', 'password123', 'utente'),
('Simone', 'Moretti', 'simone.moretti@email.com', 'password123', 'utente'),
('Martina', 'Barbieri', 'martina.barbieri@email.com', 'password123', 'utente'),
('Federico', 'Fontana', 'federico.fontana@email.com', 'password123', 'utente'),
('Alice', 'Santoro', 'alice.santoro@email.com', 'password123', 'utente'),
('Gabriele', 'Marini', 'gabriele.marini@email.com', 'password123', 'utente'),
('Giulia', 'Rizzo', 'giulia.rizzo@email.com', 'password123', 'utente'),
('Tommaso', 'Lombardo', 'tommaso.lombardo@email.com', 'password123', 'utente'),
('Giacomo', 'Patti', 'giacomo.patti@email.com', 'password123', 'utente'),
('Beatrice', 'Villa', 'beatrice.villa@email.com', 'password123', 'utente');

INSERT INTO torneo (nome, gioco, descrizione, data_inizio_iscrizioni, data_fine_iscrizioni, max_partecipanti, data_inizio_torneo, stato) VALUES
('ITS League FC24', 'EA Sports FC 24', 'Torneo di calcio 1v1 aperto a tutti gli studenti dell''istituto.', '2026-10-01', '2026-10-20', 16, '2026-10-25', 'aperto'),
('Tekken 8 ITS Championship', 'Tekken 8', 'Sfida 1v1 ad eliminazione diretta.', '2026-10-10', '2026-10-30', 16, '2026-11-05', 'aperto'),
('Super Smash Bros ITS Ultimate', 'Super Smash Bros. Ultimate', 'Torneo 1v1 3-stock senza oggetti.', '2026-09-15', '2026-10-05', 16, '2026-10-10', 'in corso'),
('Street Fighter 6 Autumn Cup', 'Street Fighter 6', 'Scontri 1v1 al meglio delle 3 partite.', '2026-09-01', '2026-09-28', 16, '2026-10-02', 'in corso'),
('ITS Chess Masters 2026', 'Scacchi', 'Torneo di scacchi rapid a tempo controllato 10+0.', '2026-08-01', '2026-08-25', 32, '2026-09-01', 'chiuso'),
('TrackMania Time Attack 2026', 'TrackMania', 'Competizione individuale 1v1 time-attack annullata.', '2026-07-01', '2026-07-15', 16, '2026-07-20', 'cancellato');

INSERT INTO iscrizione (utente_id, torneo_id, stato) VALUES
(3, 1, 'confermata'), (4, 1, 'confermata'), (5, 1, 'confermata'), (6, 1, 'confermata'),
(7, 2, 'confermata'), (8, 2, 'confermata'),
(3, 3, 'confermata'), (9, 3, 'confermata'), (10, 3, 'confermata'), (11, 3, 'confermata'),
(12, 4, 'confermata'), (13, 4, 'confermata'), (14, 4, 'in_attesa'), (15, 4, 'confermata'),
(16, 5, 'confermata'), (17, 5, 'confermata'), (18, 5, 'confermata'), (19, 5, 'confermata');

INSERT INTO partita (torneo_id, giocatore1, giocatore2, turno, dataora, punteggio1, punteggio2, stato) VALUES
(3, 3, 9, 1, '2026-10-11 15:00:00', 3, 1, 'completata'),
(3, 10, 11, 1, '2026-10-13 18:00:00', NULL, NULL, 'da_giocare'),
(5, 16, 17, 1, '2026-09-01 10:00:00', 1, 0, 'completata'),
(5, 18, 19, 1, '2026-09-01 11:00:00', 0, 1, 'completata'),
(5, 16, 19, 2, '2026-09-02 16:00:00', 1, 0, 'completata');


INSERT INTO configurazione (chiave, valore, descrizione) 
VALUES (
    'max_tornei_attivi_utente', 
    '3', 
    'Numero massimo di tornei contemporaneamente attivi a cui un utente puo iscriversi o rimanere in lista d attesa'
);