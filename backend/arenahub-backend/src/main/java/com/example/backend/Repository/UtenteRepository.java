package com.example.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.Model.Utente;

public interface UtenteRepository extends JpaRepository<Utente, Integer> {
	Optional<Utente> findByEmail(String email);

	List<Utente> iscrittiDelTorneo(int torneoId);
}