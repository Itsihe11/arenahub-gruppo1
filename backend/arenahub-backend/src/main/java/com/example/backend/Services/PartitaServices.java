package com.example.backend.Services;

import com.example.backend.Model.Partita;
import com.example.backend.Repository.PartitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PartitaServices {

	@Autowired PartitaRepository partitaRepository;

    
    public PartitaServices(PartitaRepository partitaRepository) {
        this.partitaRepository = partitaRepository;
    }

    public List<Partita> getAllPartite() {
        return partitaRepository.findAll();
    }

    public Optional<Partita> getPartitaById(Integer id) {
        return partitaRepository.findById(id);
    }

    public List<Partita> getPartiteByTorneo(Integer torneoId) {
        return partitaRepository.findByTorneoId(torneoId);
    }

    public List<Partita> getPartiteByUtente(Integer utenteId) {
        return partitaRepository.findByGiocatore1IdOrGiocatore2Id(utenteId, utenteId);
    }

    public List<Partita> getPartiteByStato(String stato) {
        return partitaRepository.findByStato(stato);
    }

    public List<Partita> getPartiteByTorneoETurno(Integer torneoId, Integer turno) {
        return partitaRepository.findByTorneoIdAndTurno(torneoId, turno);
    }

    public Partita salvaPartita(Partita partita) {
        return partitaRepository.save(partita);
    }

    public Partita aggiornaRisultato(Integer id, Integer punteggio1, Integer punteggio2, String stato) {
        Optional<Partita> partitaOptional = partitaRepository.findById(id);
        if (partitaOptional.isPresent()) {
            Partita partita = partitaOptional.get();
            partita.setPunteggio1(punteggio1);
            partita.setPunteggio2(punteggio2);
            partita.setStato(stato);
            return partitaRepository.save(partita);
        }
        return null;
    }

    public void eliminaPartita(Integer id) {
        partitaRepository.deleteById(id);
    }
}