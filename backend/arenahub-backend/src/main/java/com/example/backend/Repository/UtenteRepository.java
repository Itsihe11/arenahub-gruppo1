package com.example.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.Model.Utente;

public interface UtenteRepository extends JpaRepository<Utente, Integer> {
	Optional<Utente> findByEmail(String email);

	
	@Query("SELECT i.utente FROM Iscrizione i WHERE i.torneo.id = :torneoId")
    List<Utente> iscrittiDelTorneo(@Param("torneoId") int torneoId);
}