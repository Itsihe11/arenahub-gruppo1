package com.example.backend.Services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.Model.Utente;
import com.example.backend.Repository.UtenteRepository;

@Service 
public class UtenteService {
    private final UtenteRepository utenti;
    
    public UtenteService(UtenteRepository utenti) {
        this.utenti = utenti;
    }

    public Utente registra(Utente u) {
        if (utenti.findByEmail(u.getEmail()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Utente già esistente");
        u.setRuolo("utente");               
        return utenti.save(u);
    }

    public Utente login(String email, String password) {
        if (email == null || password == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dati mancanti");
        return utenti.findByEmail(email)
            .filter(u -> u.getPassword().equals(password))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non valide"));
    }

    public Utente utenteCorrente(int id) {
        return utenti.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non autenticato"));
    }

    public void richiediAdmin(int id) {
        if (!"admin".equals(utenteCorrente(id).getRuolo()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accesso negato");
    }
}
