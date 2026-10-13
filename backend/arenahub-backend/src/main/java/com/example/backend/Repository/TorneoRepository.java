package com.example.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.Model.Torneo;

import jakarta.transaction.Transactional;

public interface TorneoRepository extends JpaRepository<Torneo, Integer> {
	List<Torneo> findAllByOrderByDataInizioTorneo();

	List<Torneo> findByStatoOrderByDataInizioTorneo(String stato);

	Optional<Torneo> findById(int id);
	
	@Query("SELECT i.torneo FROM Iscrizione i WHERE i.utente.id = :utenteId")
    List<Torneo> torneiDellUtente(@Param("utenteId") int utenteId);
	

}
