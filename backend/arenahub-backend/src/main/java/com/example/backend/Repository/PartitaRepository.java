package com.example.backend.Repository;

import com.example.backend.Model.Partita;
import com.example.backend.Model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartitaRepository extends JpaRepository<Partita, Integer> {

    List<Partita> findByTorneoId(Integer torneoId);

    List<Partita> findByGiocatore1OrGiocatore2(Utente giocatore1, Utente giocatore2);

    List<Partita> findByGiocatore1IdOrGiocatore2Id(Integer utenteId1, Integer utenteId2);

    List<Partita> findByStato(String stato);

    List<Partita> findByTorneoIdAndStato(Integer torneoId, String stato);

    List<Partita> findByTorneoIdAndTurno(Integer torneoId, Integer turno);
}