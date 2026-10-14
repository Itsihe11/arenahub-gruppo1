package com.example.backend.Services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String, Object> registra(Map<String, Object> p) {
        String nome = testo(p, "nome");
        String cognome = testo(p, "cognome");
        String email = testo(p, "email");
        String password = testo(p, "password");

        if (nome == null || cognome == null || email == null || password == null
                || !email.contains("@") || password.length() < 8)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dati non validi");

        if (utenti.findByEmail(email).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Utente già esistente");

        Utente u = new Utente();                
        u.setNome(nome);
        u.setCognome(cognome);
        u.setEmail(email);
        u.setPassword(password);
        u.setRuolo("utente");
        return mappa(utenti.save(u), true);
    }

    public Map<String, Object> login(Map<String, Object> p) {
        String email = testo(p, "email");
        String password = testo(p, "password");
        if (email == null || password == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dati mancanti");

        Utente u = utenti.findByEmail(email)
                .filter(x -> x.getPassword().equals(password))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non valide"));

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("token", String.valueOf(u.getId()));    
        r.put("utente", mappa(u, false));
        return r;
    }

    public List<Map<String, Object>> lista(Integer uid) {
        richiediAdmin(uid);
        List<Map<String, Object>> r = new ArrayList<>();
        for (Utente u : utenti.findAll()) r.add(mappa(u, true));
        return r;
    }

    public Map<String, Object> me(Integer uid) {
        return mappa(utenteCorrente(uid), true);
    }

    public Integer idDaHeader(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        try {
            return Integer.parseInt(auth.substring(7).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Utente utenteCorrente(Integer id) {
        if (id == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non autenticato");
        return utenti.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non autenticato"));
    }

    public void richiediAdmin(Integer id) {
        if (!"admin".equals(utenteCorrente(id).getRuolo()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accesso negato");
    }

    private Map<String, Object> mappa(Utente u, boolean conEmail) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("nome", u.getNome());
        m.put("cognome", u.getCognome());
        if (conEmail) m.put("email", u.getEmail());
        m.put("ruolo", u.getRuolo());
        return m;
    }

    private String testo(Map<String, Object> p, String chiave) {
        Object v = p == null ? null : p.get(chiave);
        return (v == null || v.toString().isBlank()) ? null : v.toString().trim();
    }
}