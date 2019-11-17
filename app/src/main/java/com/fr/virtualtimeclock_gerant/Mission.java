package com.fr.virtualtimeclock_gerant;

import java.util.Date;

public class Mission {
    private String titre;
    private String description;
    private String lieu;
    private Date debut;
    private Date fin;

    public Mission() {
        //empty constructor needed
    }

    public Mission(String title, String description, String lieu, Date debut, Date fin) {
        this.titre = title;
        this.description = description;
        this.lieu = lieu;
        this.debut = debut;
        this.fin = fin;
    }

    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public String getLieu() { return lieu; }
    public Date getDebut() { return debut; }
    public Date getFin() { return fin; }
}