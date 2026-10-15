package com.example.backend.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "configurazione")
public class Configurazione {

    @Id
    private String chiave;
    private String valore;
    private String descrizione;

    public Configurazione() {}

    public Configurazione(String chiave, String valore, String descrizione) {
        this.chiave = chiave;
        this.valore = valore;
        this.descrizione = descrizione;
    }

    public String getChiave() {
        return chiave;
    }

    public void setChiave(String chiave) {
        this.chiave = chiave;
    }

    public String getValore() {
        return valore;
    }

    public void setValore(String valore) {
        this.valore = valore;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }
}