package com.example.backend.Services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        List<Partita> lista = partitaRepo.findByTorneoIdOrderByTurnoAscIdAsc(torneoId);
        List<Map<String, Object>> risultato = new ArrayList<>();
        for (Partita p : lista) {
            risultato.add(mappaPartita(p));
        }
        return risultato;
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public List<Map<String, Object>> generaTabellone(Integer uid, Integer torneoId) {
        utenti.richiediAdmin(uid);
        Torneo torneo = torneoService.trova(torneoId);

        List<Partita> partiteEsistenti = partitaRepo.findByTorneoIdOrderByTurnoAscIdAsc(torneoId);

        if (partiteEsistenti.isEmpty()) {
            List<Utente> iscritti = utenteRepo.iscrittiDelTorneo(torneoId);

            if (iscritti.size() < 2) {
                torneo.setStato("chiuso");
                torneoRepo.save(torneo);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Nessun turno generabile: iscritti insufficienti (< 2). Il torneo è stato impostato su 'chiuso'.");
            }

            torneo.setStato("in corso");
            torneoRepo.save(torneo);

            List<Utente> giocatoriShuffled = new ArrayList<>(iscritti);
            Collections.shuffle(giocatoriShuffled);

            List<Partita> nuovePartite = creaNuovoTurno(torneo, giocatoriShuffled, 1);
            partitaRepo.saveAll(nuovePartite);

        } else {
            int maxTurno = partiteEsistenti.stream()
                    .mapToInt(Partita::getTurno)
                    .max()
                    .orElse(1);

            List<Partita> ultimoTurnoPartite = partiteEsistenti.stream()
                    .filter(p -> p.getTurno() == maxTurno)
                    .toList();

            boolean tutteCompletate = ultimoTurnoPartite.stream()
                    .allMatch(p -> "completata".equalsIgnoreCase(p.getStato()) && p.getVincitore() != null);

            if (!tutteCompletate) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Devi prima completare tutte le partite e decretare i vincitori del Turno " + maxTurno);
            }

            List<Utente> vincitori = ultimoTurnoPartite.stream()
                    .map(Partita::getVincitore)
                    .toList();

            if (vincitori.size() < 2) {
                torneo.setStato("chiuso");
                torneoRepo.save(torneo);

                String vincitoreFinale = !vincitori.isEmpty()
                        ? vincitori.get(0).getNome() + " " + vincitori.get(0).getCognome()
                        : "Nessuno";

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Torneo concluso! Vincitore del torneo: " + vincitoreFinale + ". Lo stato è stato impostato su 'chiuso'.");
            }

            List<Utente> vincitoriShuffled = new ArrayList<>(vincitori);
            Collections.shuffle(vincitoriShuffled);

            List<Partita> prossimePartite = creaNuovoTurno(torneo, vincitoriShuffled, maxTurno + 1);
            partitaRepo.saveAll(prossimePartite);
        }

        return getPartiteByTorneo(torneoId);
    }

    @Transactional
    public List<Map<String, Object>> resetTabellone(Integer uid, Integer torneoId) {
        utenti.richiediAdmin(uid);
        partitaRepo.deleteByTorneoId(torneoId);
        return new ArrayList<>();
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

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public Map<String, Object> salvaRisultato(Integer uid, Integer partitaId, Integer p1, Integer p2, String stato) {
        utenti.richiediAdmin(uid);
        Partita p = partitaRepo.findById(partitaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partita non trovata"));

        p.setPunteggio1(p1);
        p.setPunteggio2(p2);

        if (stato != null && !stato.isBlank()) {
            p.setStato(stato);
        } else {
            p.setStato("completata");
        }

        if ("completata".equalsIgnoreCase(p.getStato()) && p1 != null && p2 != null) {
            if (p1 > p2) {
                p.setVincitore(p.getGiocatore1());
            } else if (p2 > p1) {
                p.setVincitore(p.getGiocatore2());
            } else {
                p.setVincitore(null);
            }
        }

        Partita salvata = partitaRepo.saveAndFlush(p);

        Torneo torneo = p.getTorneo();
        List<Partita> tutteLePartite = partitaRepo.findByTorneoIdOrderByTurnoAscIdAsc(torneo.getId());

        int maxTurno = tutteLePartite.stream().mapToInt(Partita::getTurno).max().orElse(1);
        List<Partita> partiteUltimoTurno = tutteLePartite.stream().filter(match -> match.getTurno() == maxTurno).toList();

        boolean ultimoTurnoCompletato = partiteUltimoTurno.stream()
                .allMatch(match -> "completata".equalsIgnoreCase(match.getStato()) && match.getVincitore() != null);

        if (ultimoTurnoCompletato && partiteUltimoTurno.size() == 1) {
            torneo.setStato("chiuso");
            torneoRepo.save(torneo);
        }

        return mappaPartita(salvata);
    }

    private Map<String, Object> mappaPartita(Partita p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("torneoId", p.getTorneo().getId());
        m.put("turno", p.getTurno());
        m.put("dataora", p.getDataora());
        m.put("punteggio1", p.getPunteggio1());
        m.put("punteggio2", p.getPunteggio2());
        m.put("stato", p.getStato());

        Map<String, Object> g1 = new LinkedHashMap<>();
        g1.put("id", p.getGiocatore1().getId());
        g1.put("nome", p.getGiocatore1().getNome());
        g1.put("cognome", p.getGiocatore1().getCognome());
        m.put("giocatore1", g1);

        Map<String, Object> g2 = new LinkedHashMap<>();
        g2.put("id", p.getGiocatore2().getId());
        g2.put("nome", p.getGiocatore2().getNome());
        g2.put("cognome", p.getGiocatore2().getCognome());
        m.put("giocatore2", g2);

        if (p.getVincitore() != null) {
            m.put("vincitoreId", p.getVincitore().getId());
        } else {
            m.put("vincitoreId", null);
        }

        return m;
    }
}