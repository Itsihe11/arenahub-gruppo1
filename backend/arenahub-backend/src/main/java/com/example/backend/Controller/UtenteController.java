package com.example.backend.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.Services.UtenteService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class UtenteController {

    private final UtenteService service;

    public UtenteController(UtenteService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> body) {
        return service.login(body);
    }

    @PostMapping("/registrazione")
    public ResponseEntity<Map<String, Object>> registrazione(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registra(body));
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
}