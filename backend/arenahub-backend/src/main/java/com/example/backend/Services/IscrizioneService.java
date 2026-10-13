package com.example.backend.Services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.Model.Iscrizione;
import com.example.backend.Model.Torneo;
import com.example.backend.Model.Utente;
import com.example.backend.Repository.IscrizioneRepository;
import com.example.backend.Repository.TorneoRepository;
import com.example.backend.Repository.UtenteRepository;

import jakarta.transaction.Transactional;

@Service
public class IscrizioneService {
	private final TorneoRepository tornei;
	private final IscrizioneRepository iscrizioni;
	private final UtenteRepository utentiRepo;
	private final UtenteService utenti;
	private final TorneoService torneoService;

	public IscrizioneService(TorneoRepository tornei, IscrizioneRepository iscrizioni, UtenteRepository utentiRepo,
			UtenteService utenti, TorneoService torneoService) {
		this.tornei = tornei;
		this.iscrizioni = iscrizioni;
		this.utentiRepo = utentiRepo;
		this.utenti = utenti;
		this.torneoService = torneoService;
	}

	@Transactional
	public Iscrizione iscrivi(int uid, int torneoId) {
		Utente u = utenti.utenteCorrente(uid);
		Torneo t = tornei.findById(torneoId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Torneo non trovato"));
		if (!t.getStato().equals("aperto"))
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Torneo chiuso");
		if (iscrizioni.countByTorneoId(torneoId) >= t.getMaxPartecipanti())
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Torneo pieno");
		if (iscrizioni.existsByTorneoIdAndUtenteId(torneoId, u.getId()))
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Utente già iscritto");

		Iscrizione i = new Iscrizione();
		i.setTorneo(t);
		i.setUtente(u);
		return iscrizioni.save(i);
	}

	@Transactional
	public void annulla(int uid, int torneoId) {
		Utente u = utenti.utenteCorrente(uid);
		Torneo t = torneoService.trova(torneoId);
		if (!t.getStato().equals("aperto"))
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Torneo non più aperto");
		if (iscrizioni.deleteByTorneoIdAndUtenteId(torneoId, u.getId()) == 0)
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Iscrizione non trovata");
	}

	public List<Torneo> mieIscrizioni(int uid) {
	    Utente u = utenti.utenteCorrente(uid);
	    List<Torneo> lista = tornei.torneiDellUtente(u.getId());
	    for (Torneo t : lista) {
	        torneoService.conIscritti(t);
	    }
	    return lista;
	}

	public List<Utente> iscritti(int uid, int torneoId) {
		utenti.richiediAdmin(uid);
		torneoService.trova(torneoId);
		return utentiRepo.iscrittiDelTorneo(torneoId);
	}
}
