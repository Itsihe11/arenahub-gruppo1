package com.example.backend.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "torneo")
public class Torneo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String nome;
	private String gioco;
	private String descrizione;
	private LocalDate dataInizioIscrizione;
	private LocalDate dataFineIscrizione;
	private int maxPartecipanti;
	private LocalDateTime dataInizioTorneo;
	private String stato;

	public Torneo() {

	}

	public Torneo(String nome, String gioco, String descrizione, LocalDate dataInizioIscrizione,
			LocalDate dataFineIscrizione, int maxPartecipanti, LocalDateTime dataInizioTorneo, String stato) {
		this.nome = nome;
		this.gioco = gioco;
		this.descrizione = descrizione;
		this.dataInizioIscrizione = dataInizioIscrizione;
		this.dataFineIscrizione = dataFineIscrizione;
		this.maxPartecipanti = maxPartecipanti;
		this.dataInizioTorneo = dataInizioTorneo;
		this.stato = stato;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getGioco() {
		return gioco;
	}

	public void setGioco(String gioco) {
		this.gioco = gioco;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	public LocalDate getDataInizioIscrizione() {
		return dataInizioIscrizione;
	}

	public void setDataInizioIscrizione(LocalDate dataInizioIscrizione) {
		this.dataInizioIscrizione = dataInizioIscrizione;
	}

	public LocalDate getDataFineIscrizione() {
		return dataFineIscrizione;
	}

	public void setDataFineIscrizione(LocalDate dataFineIscrizione) {
		this.dataFineIscrizione = dataFineIscrizione;
	}

	public int getMaxPartecipanti() {
		return maxPartecipanti;
	}

	public void setMaxPartecipanti(int maxPartecipanti) {
		this.maxPartecipanti = maxPartecipanti;
	}

	public LocalDateTime getDataInizioTorneo() {
		return dataInizioTorneo;
	}

	public void setDataInizioTorneo(LocalDateTime dataInizioTorneo) {
		this.dataInizioTorneo = dataInizioTorneo;
	}

	public String getStato() {
		return stato;
	}

	public void setStato(String stato) {
		this.stato = stato;
	}

}
