package com.example.backend.Services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.Dto.LoginRequest;
import com.example.backend.Dto.RegistrazioneRequest;
import com.example.backend.Model.Configurazione;
import com.example.backend.Model.Utente;
import com.example.backend.Repository.ConfigurazioneRepository;
import com.example.backend.Repository.IscrizioneRepository;
import com.example.backend.Repository.UtenteRepository;

@Service
public class UtenteService {

    private static final String CHIAVE_MAX_TORNEI = "max_tornei_attivi_utente";

    private final UtenteRepository utenti;
    private final IscrizioneRepository iscrizioneRepository;
    private final ConfigurazioneRepository configurazioneRepository;
    private final PasswordEncoder passwordEncoder;

    public UtenteService(
            UtenteRepository utenti,
            IscrizioneRepository iscrizioneRepository,
            ConfigurazioneRepository configurazioneRepository,
            PasswordEncoder passwordEncoder) {
        this.utenti = utenti;
        this.iscrizioneRepository = iscrizioneRepository;
        this.configurazioneRepository = configurazioneRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> registra(RegistrazioneRequest req) {
        if (utenti.findByEmail(req.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Utente già esistente");
        }

        Utente u = new Utente();                
        u.setNome(req.getNome());
        u.setCognome(req.getCognome());
        u.setEmail(req.getEmail());
        
      
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRuolo("utente");
        
        return mappa(utenti.save(u), true);
    }

    public Map<String, Object> login(LoginRequest req) {
        Utente u = utenti.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non valide"));

        
        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non valide");
        }

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

    public Utente trovaDaHeader(String authHeader) {
        Integer id = idDaHeader(authHeader);
        if (id == null) return null;
        return utenti.findById(id).orElse(null);
    }

    public Utente utenteCorrente(Integer id) {
        if (id == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non autenticato");
        return utenti.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non autenticato"));
    }

    public void richiediAdmin(Integer id) {
        if (!"admin".equalsIgnoreCase(utenteCorrente(id).getRuolo()))
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

    public int getMaxTorneiAttivi() {
        return configurazioneRepository.findById(CHIAVE_MAX_TORNEI)
                .map(c -> {
                    try {
                        return Integer.parseInt(c.getValore());
                    } catch (NumberFormatException e) {
                        return 3;
                    }
                })
                .orElse(3);
    }

    @Transactional
    public void setMaxTorneiAttivi(int max) {
        Configurazione config = configurazioneRepository.findById(CHIAVE_MAX_TORNEI)
                .orElse(new Configurazione(CHIAVE_MAX_TORNEI, String.valueOf(max), "Numero massimo di tornei contemporanei"));

        config.setValore(String.valueOf(max));
        configurazioneRepository.saveAndFlush(config);
    }

    public boolean isAdmin(String authHeader) {
        Utente u = trovaDaHeader(authHeader);
        return u != null && "admin".equalsIgnoreCase(u.getRuolo());
    }

    public int getTorneiAttiviConteggiati(Integer utenteId) {
        if (utenteId == null) return 0;
        return iscrizioneRepository.countByUtenteIdAndTorneoStatoAperto(utenteId);
    }
}