package com.fr.virtualtimeclock_gerant;

import java.util.Date;

public class EmployeeInMission {

    private Date date;
    private Boolean estPresent;

    public EmployeeInMission() {
        //empty constructor needed
    }

    public EmployeeInMission(Date date, Boolean estPresent) {
        this.date = date;
        this.estPresent = estPresent;
    }

    public Date getDate() { return date; }
    public Boolean getEstPresent() { return estPresent; }
}