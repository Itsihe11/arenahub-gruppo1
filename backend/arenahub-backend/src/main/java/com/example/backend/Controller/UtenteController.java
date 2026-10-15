package com.example.backend.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.Dto.LoginRequest;
import com.example.backend.Dto.RegistrazioneRequest;
import com.example.backend.Services.UtenteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class UtenteController {

	private final UtenteService service;

    public UtenteController(UtenteService utenteService) {
        this.service = utenteService;
    }

    @PostMapping("/registrazione")
    public ResponseEntity<Map<String, Object>> registrazione(@Valid @RequestBody RegistrazioneRequest req) {
        Map<String, Object> risposta = service.registra(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(risposta);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        Map<String, Object> risposta = service.login(req);
        return ResponseEntity.ok(risposta);
    }

    @GetMapping("/utenti")
    public List<Map<String, Object>> lista(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        return service.lista(service.idDaHeader(auth));
    }

    @GetMapping("/utenti/me")
    public Map<String, Object> me(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        return service.me(service.idDaHeader(auth));
    }

 
    @GetMapping("/admin/maxTornei")
    public ResponseEntity<?> getMaxTornei(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth == null || auth.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("errore", "Non autenticato"));
        }

        if (!service.isAdmin(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("errore", "Accesso negato"));
        }

        int max = service.getMaxTorneiAttivi();
        return ResponseEntity.ok(Map.of("maxTorneiAttivi", max));
    }


    @PutMapping("/admin/maxTornei")
    public ResponseEntity<?> setMaxTornei(@Valid
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Object> body) {

        if (auth == null || auth.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("errore", "Non autenticato"));
        }

        if (!service.isAdmin(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("errore", "Accesso negato"));
        }

        if (body == null || !body.containsKey("maxTorneiAttivi")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errore", "Numero mancante o non valido"));
        }

        try {
            int max = Integer.parseInt(body.get("maxTorneiAttivi").toString());
            if (max < 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("errore", "Numero mancante o non valido"));
            }

            service.setMaxTorneiAttivi(max);
            return ResponseEntity.ok(Map.of("maxTorneiAttivi", max));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errore", "Numero mancante o non valido"));
        }
    }


    @GetMapping("/utente/me/statoIscrizione")
    public ResponseEntity<?> getStatoIscrizione(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth == null || auth.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("errore", "Non autenticato"));
        }

        Integer utenteId = service.idDaHeader(auth);
        if (utenteId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("errore", "Utente non trovato"));
        }

        int torneiAttivi = service.getTorneiAttiviConteggiati(utenteId);
        int limiteMassimo = service.getMaxTorneiAttivi();
        boolean canRegister = torneiAttivi < limiteMassimo;

        Map<String, Object> risposta = new HashMap<>();
        risposta.put("torneiAttiviConteggiati", torneiAttivi);
        risposta.put("limiteMassimo", limiteMassimo);
        risposta.put("canRegister", canRegister);

        return ResponseEntity.ok(risposta);
    }
}