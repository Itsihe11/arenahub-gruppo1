package com.example.backend.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class MaxTorneiRequest {

    @NotNull(message = "Numero mancante o non valido")
    @Min(value = 1, message = "Numero mancante o non valido")
    private Integer maxTorneiAttivi;

    public Integer getMaxTorneiAttivi() { return maxTorneiAttivi; }
    public void setMaxTorneiAttivi(Integer maxTorneiAttivi) { this.maxTorneiAttivi = maxTorneiAttivi; }
}