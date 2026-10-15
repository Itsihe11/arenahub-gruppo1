package com.example.backend.Dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TorneoCreateRequest {

    @NotBlank(message = "Dati non validi")
    private String nome;

    @NotBlank(message = "Dati non validi")
    private String gioco;

    @NotBlank(message = "Dati non validi")
    private String descrizione;

    @NotNull(message = "Dati non validi")
    private LocalDateTime dataTorneo;

    @NotNull(message = "Dati non validi")
    private LocalDate dataInizioIscrizione;

    @NotNull(message = "Dati non validi")
    private LocalDate dataFineIscrizione;

    @NotNull(message = "Dati non validi")
    @Min(value = 1, message = "Dati non validi")
    private Integer postiMax;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getGioco() { return gioco; }
    public void setGioco(String gioco) { this.gioco = gioco; }
    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    public LocalDateTime getDataTorneo() { return dataTorneo; }
    public void setDataTorneo(LocalDateTime dataTorneo) { this.dataTorneo = dataTorneo; }
    public LocalDate getDataInizioIscrizione() { return dataInizioIscrizione; }
    public void setDataInizioIscrizione(LocalDate dataInizioIscrizione) { this.dataInizioIscrizione = dataInizioIscrizione; }
    public LocalDate getDataFineIscrizione() { return dataFineIscrizione; }
    public void setDataFineIscrizione(LocalDate dataFineIscrizione) { this.dataFineIscrizione = dataFineIscrizione; }
    public Integer getPostiMax() { return postiMax; }
    public void setPostiMax(Integer postiMax) { this.postiMax = postiMax; }
}