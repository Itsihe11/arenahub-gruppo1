package com.example.backend.Model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table(name = "iscrizione")
public class Iscrizione {
	
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id 
	private int id;
	
	@ManyToOne(fetch = FetchType.LAZY) 
	@JoinColumn(name = "torneo_id") 
	private Torneo torneo;
	
	@ManyToOne(fetch = FetchType.LAZY) 
	@JoinColumn(name = "utente_id") 
	private Utente utente;
	
	@Column(name = "data_iscrizione")
	private LocalDate data = LocalDate.now();
	
	private String stato;
	
	public Iscrizione() {
		
	}

	public Iscrizione(Torneo torneo, Utente utente, LocalDate data, String stato) {
		this.torneo = torneo;
		this.utente = utente;
		this.data = data;
		this.stato = stato;
	}

	public int getId() {
		return id;
	}

	public Torneo getTorneo() {
		return torneo;
	}

	public void setTorneo(Torneo torneo) {
		this.torneo = torneo;
	}

	public Utente getUtente() {
		return utente;
	}

	public void setUtente(Utente utente) {
		this.utente = utente;
	}

	public LocalDate getData() {
		return data;
	}

	public void setData(LocalDate data) {
		this.data = data;
	}

	public String getStato() {
		return stato;
	}

	public void setStato(String stato) {
		this.stato = stato;
	}

	public Integer getTorneoId() {
		return torneo != null ? torneo.getId() : null;
	}

	public Integer getUtenteId() {
		return utente != null ? utente.getId() : null;
	}
}