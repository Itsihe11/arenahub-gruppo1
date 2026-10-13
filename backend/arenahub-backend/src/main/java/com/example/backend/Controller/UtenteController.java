package com.example.backend.Controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Model.Utente;
import com.example.backend.Repository.IscrizioneRepository;
import com.example.backend.Repository.UtenteRepository;

@RestController
@RequestMapping("/api/utenti")
@CrossOrigin(origins = "http://localhost:4200")
public class UtenteController {

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private IscrizioneRepository iscrizioneRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenziali) {
        String email = credenziali.get("email");
        String password = credenziali.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body("Email e password richieste.");
        }

        Optional<Utente> utenteOpt = utenteRepository.findByEmail(email);

        if (utenteOpt.isPresent()) {
            Utente utente = utenteOpt.get();
            if (utente.getPassword().equals(password)) {
              
                List<Integer> torneoIds = iscrizioneRepository.findTorneoIdsByUtenteId(utente.getId());
                utente.setTorneiIscrittiIds(torneoIds);

                return ResponseEntity.ok(utente);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenziali non valide.");
    }

    @PostMapping("/registrazione")
    public ResponseEntity<?> registrazione(@RequestBody Utente nuovoUtente) {
        if (utenteRepository.findByEmail(nuovoUtente.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email già registrata.");
        }

        if (nuovoUtente.getRuolo() == null) {
            nuovoUtente.setRuolo("utente");
        }

        Utente salvato = utenteRepository.save(nuovoUtente);
        return ResponseEntity.ok(salvato);
    }
}