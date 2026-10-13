package com.example.backend.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.Model.Iscrizione;

public interface IscrizioneRepository extends JpaRepository<Iscrizione, Integer> {
	long countByTorneoId(int torneoId);

	boolean existsByTorneoIdAndUtenteId(int torneoId, int utenteId);

	long deleteByTorneoIdAndUtenteId(int torneoId, int utenteId);

	List<Iscrizione> findByUtenteIdOrderByTorneoDataInizioTorneo(int utenteId);

	List<Iscrizione> findByTorneoIdOrderByData(int torneoId);
	
	List<Iscrizione> findByUtenteId(int utenteId);
 }
