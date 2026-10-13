package com.example.backend.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.Model.Partita;

@Repository
public interface PartitaRepository extends JpaRepository<Partita, Integer> {
    List<Partita> findByTorneoIdOrderByTurnoAscIdAsc(Integer torneoId);
    void deleteByTorneoId(Integer torneoId);
}