package com.example.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.backend.Model.Configurazione;

@Repository
public interface ConfigurazioneRepository extends JpaRepository<Configurazione, String> {
}