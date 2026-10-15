package com.example.backend.Dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class TorneoCreateRequest {

    @NotBlank(message = "Il nome è obbligatorio")
    private String nome;

    @NotBlank(message = "Il gioco è obbligatorio")
    private String gioco;

    @NotNull(message = "La data è obbligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataTorneo;

    @NotNull(message = "Il numero di posti è obbligatorio")
    @Min(value = 2, message = "Inserire almeno 2 posti")
    private Integer postiMax;

    private String descrizione;

   
    private String stato = "aperto";

    public TorneoCreateRequest() {}


    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getGioco() { return gioco; }
    public void setGioco(String gioco) { this.gioco = gioco; }

    public LocalDate getDataTorneo() { return dataTorneo; }
    public void setDataTorneo(LocalDate dataTorneo) { this.dataTorneo = dataTorneo; }

    public Integer getPostiMax() { return postiMax; }
    public void setPostiMax(Integer postiMax) { this.postiMax = postiMax; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }
}