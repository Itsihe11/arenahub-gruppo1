package com.example.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.Model.Torneo;

public interface TorneoRepository extends JpaRepository<Torneo, Integer> {
	List<Torneo> findAllByOrderByDataInizioTorneo();

	List<Torneo> findByStatoOrderByDataInizioTorneo(String stato);

	Optional<Torneo> findById(int id);

}
