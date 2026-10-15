package com.example.backend.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Services.TorneoService;
import com.example.backend.Services.UtenteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tornei")
@CrossOrigin(origins = "http://localhost:4200")
public class TorneoController {

	private final TorneoService torneoService;
	private final UtenteService utenteService;

	public TorneoController(TorneoService torneoService, UtenteService utenteService) {
		this.torneoService = torneoService;
		this.utenteService = utenteService;
	}

	@GetMapping
	public List<Map<String, Object>> elenco(@RequestParam(required = false) String stato,
			@RequestParam(required = false) String gioco, @RequestParam(required = false) String data) {
		return torneoService.elenco(stato, gioco, data);
	}

	@GetMapping("/{id}")
	public Map<String, Object> dettaglio(@PathVariable int id) {
		return torneoService.dettaglio(id);
	}

	@PostMapping
	public ResponseEntity<Map<String, Object>> crea(
			@Valid @RequestHeader(value = "Authorization", required = false) String auth,
			@RequestBody Map<String, Object> payload) {

		Map<String, Object> r = torneoService.crea(uid(auth), payload);
		return ResponseEntity.status(HttpStatus.CREATED).body(r);
	}

	@PutMapping("/{id}")
	public Map<String, Object> modifica(@Valid @RequestHeader(value = "Authorization", required = false) String auth,
			@PathVariable int id, @RequestBody Map<String, Object> payload) {
		return torneoService.modifica(uid(auth), id, payload);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> elimina(@RequestHeader(value = "Authorization", required = false) String auth,
			@PathVariable int id) {
		torneoService.elimina(uid(auth), id);
		return ResponseEntity.noContent().build();
	}

	private Integer uid(String auth) {
	    return utenteService.idDaHeader(auth);
	}

}