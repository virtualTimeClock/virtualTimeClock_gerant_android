package com.fr.virtualtimeclock_gerant;

import android.app.Application;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends Application {

    // Vérifie si l'utilisateur est déjà connecté pour le faire directement arriver sur l'activité principale
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            startActivity(new Intent(Home.this, StartingActivity.class));
        }
    }
}
