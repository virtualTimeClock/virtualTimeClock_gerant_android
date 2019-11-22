package com.fr.virtualtimeclock_gerant;

import java.util.Date;

public class Employee {
    private String nom;
    private String prenom;
    private Date dateNaissance;

    public Employee() {
        //empty constructor needed
    }

    public Employee(String nom, String prenom) {
        this.nom = nom;
        this.prenom = prenom;
    }

    public Employee(String nom, String prenom, Date dateNaissance) {
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
    }

    public void setNom(String nom) {this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setDateNaissance(Date dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public Date getDateNaissance() { return dateNaissance; }
}