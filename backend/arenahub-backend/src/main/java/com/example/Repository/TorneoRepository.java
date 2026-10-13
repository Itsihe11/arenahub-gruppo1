package com.example.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Model.Torneo;

public interface TorneoRepository extends JpaRepository<Torneo, Integer> {
    List<Torneo> findAllByOrderByDataOra();
    List<Torneo> findByStatoOrderByDataOra(String stato);

    Optional<Torneo> findByIdForUpdate(Long id);
}
