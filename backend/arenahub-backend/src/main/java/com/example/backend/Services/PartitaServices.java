package com.example.backend.Services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.Model.Partita;
import com.example.backend.Model.Torneo;
import com.example.backend.Model.Utente;
import com.example.backend.Repository.PartitaRepository;
import com.example.backend.Repository.TorneoRepository;
import com.example.backend.Repository.UtenteRepository;

@Service
public class PartitaServices {

    private static final Set<String> STATI_PARTITA = Set.of("da_giocare", "completata");

    private final PartitaRepository partitaRepo;
    private final TorneoService torneoService;
    private final TorneoRepository torneoRepo;
    private final UtenteRepository utenteRepo;
    private final UtenteService utenti;

    public PartitaServices(PartitaRepository partitaRepo, TorneoService torneoService,
                           TorneoRepository torneoRepo, UtenteRepository utenteRepo,
                           UtenteService utenti) {
        this.partitaRepo = partitaRepo;
        this.torneoService = torneoService;
        this.torneoRepo = torneoRepo;
        this.utenteRepo = utenteRepo;
        this.utenti = utenti;
    }


    public List<Map<String, Object>> getPartiteByTorneo(Integer torneoId) {
        torneoService.trova(torneoId);                       
        return elencoPartite(torneoId);
    }


    @Transactional
    public List<Map<String, Object>> generaTabellone(Integer uid, Integer torneoId) {
        utenti.richiediAdmin(uid);                           
        Torneo torneo = torneoService.trova(torneoId);       
        if ("concluso".equals(torneo.getStato()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Torneo concluso");

        List<Partita> esistenti = partitaRepo.findByTorneoIdOrderByTurnoAscIdAsc(torneoId);

        if (esistenti.isEmpty()) {
            List<Utente> iscritti = utenteRepo.iscrittiDelTorneo(torneoId);
            if (iscritti.size() < 2)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Iscritti insufficienti");

            List<Utente> mescolati = new ArrayList<>(iscritti);
            Collections.shuffle(mescolati);
            partitaRepo.saveAll(creaNuovoTurno(torneo, mescolati, 1));

        } else {
            int maxTurno = esistenti.stream().mapToInt(Partita::getTurno).max().orElse(1);
            List<Partita> ultimoTurno = esistenti.stream()
                    .filter(p -> p.getTurno() == maxTurno)
                    .toList();

            boolean tutteCompletate = ultimoTurno.stream()
                    .allMatch(p -> "completata".equals(p.getStato()) && p.getVincitore() != null);
            if (!tutteCompletate)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Tabellone già generato");

            List<Utente> vincitori = ultimoTurno.stream().map(Partita::getVincitore).toList();
            if (vincitori.size() < 2)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Torneo concluso");

            List<Utente> mescolati = new ArrayList<>(vincitori);
            Collections.shuffle(mescolati);
            partitaRepo.saveAll(creaNuovoTurno(torneo, mescolati, maxTurno + 1));
        }

        return elencoPartite(torneoId);
    }


    @Transactional
    public Map<String, Object> salvaRisultato(Integer uid, Integer partitaId, Map<String, Object> body) {
        utenti.richiediAdmin(uid);                                            
        Partita p = partitaRepo.findById(partitaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partita non trovata"));

        int p1 = punteggio(body, "punteggio1");                             
        int p2 = punteggio(body, "punteggio2");
        String stato = "completata";
        Object s = body.get("stato");
        if (s != null && !s.toString().isBlank()) stato = s.toString().trim();
        if (!STATI_PARTITA.contains(stato))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dati non validi");

        Torneo torneo = p.getTorneo();
        if ("concluso".equals(torneo.getStato()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Torneo concluso");   

        p.setPunteggio1(p1);
        p.setPunteggio2(p2);
        p.setStato(stato);
        if ("completata".equals(stato)) {
            p.setVincitore(p1 > p2 ? p.getGiocatore1() : (p2 > p1 ? p.getGiocatore2() : null));
        } else {
            p.setVincitore(null);
        }
        Partita salvata = partitaRepo.saveAndFlush(p);

        List<Partita> tutte = partitaRepo.findByTorneoIdOrderByTurnoAscIdAsc(torneo.getId());
        int maxTurno = tutte.stream().mapToInt(Partita::getTurno).max().orElse(1);
        List<Partita> ultimoTurno = tutte.stream().filter(m -> m.getTurno() == maxTurno).toList();
        boolean completato = ultimoTurno.stream()
                .allMatch(m -> "completata".equals(m.getStato()) && m.getVincitore() != null);
        if (completato && ultimoTurno.size() == 1) {
            torneo.setStato("concluso");
            torneoRepo.save(torneo);
        }

        return mappaPartita(salvata);
    }


    @Transactional
    public List<Map<String, Object>> resetTabellone(Integer uid, Integer torneoId) {
        utenti.richiediAdmin(uid);                           
        Torneo torneo = torneoService.trova(torneoId);      
        if ("concluso".equals(torneo.getStato()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Torneo concluso");
        partitaRepo.deleteByTorneoId(torneoId);
        return new ArrayList<>();
    }


    private List<Map<String, Object>> elencoPartite(Integer torneoId) {
        List<Map<String, Object>> risultato = new ArrayList<>();
        for (Partita p : partitaRepo.findByTorneoIdOrderByTurnoAscIdAsc(torneoId)) {
            risultato.add(mappaPartita(p));
        }
        return risultato;
    }

    private int punteggio(Map<String, Object> body, String chiave) {
        Object v = (body == null) ? null : body.get(chiave);
        if (v == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dati non validi");
        int n;
        try {
            n = Integer.parseInt(v.toString().trim());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dati non validi");
        }
        if (n < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dati non validi");
        return n;
    }

    private List<Partita> creaNuovoTurno(Torneo torneo, List<Utente> giocatori, int turno) {
        List<Partita> nuove = new ArrayList<>();
        for (int i = 0; i < giocatori.size() - 1; i += 2) {
            Partita p = new Partita();
            p.setTorneo(torneo);
            p.setGiocatore1(giocatori.get(i));
            p.setGiocatore2(giocatori.get(i + 1));
            p.setTurno(turno);
            p.setStato("da_giocare");
            p.setDataora(LocalDateTime.now().plusDays(1));  
            nuove.add(p);
        }
        return nuove;
    }

    private Map<String, Object> mappaPartita(Partita p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("torneoId", p.getTorneo().getId());
        m.put("turno", p.getTurno());
        m.put("giocatore1", mappaGiocatore(p.getGiocatore1()));
        m.put("giocatore2", mappaGiocatore(p.getGiocatore2()));
        m.put("punteggio1", p.getPunteggio1());
        m.put("punteggio2", p.getPunteggio2());
        m.put("stato", p.getStato());
        return m;
    }

    private Map<String, Object> mappaGiocatore(Utente u) {
        Map<String, Object> g = new LinkedHashMap<>();
        g.put("id", u.getId());
        g.put("nome", u.getNome());
        g.put("cognome", u.getCognome());
        return g;
    }
}