package com.fr.virtualtimeclock_gerant;


import java.util.Date;

public class CompleteEmployeeInMission {
    private String nom;
    private String prenom;
    private Date date;
    private Boolean estPresent;

    public CompleteEmployeeInMission() {
        //empty constructor needed
    }

    public CompleteEmployeeInMission(String nom, String prenom, Date date, Boolean estPresent) {
        this.nom = nom;
        this.prenom = prenom;
        this.date = date;
        this.estPresent = estPresent;
    }

    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setDate(Date date) { this.date = date; }
    public void setEstPresent(Boolean estPresent) { this.estPresent = estPresent; }

    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public Date getDate() { return date; }
    public Boolean getEstPresent() { return estPresent; }
}