package com.fr.virtualtimeclock_gerant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CompleteMissionActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CompleteMissionActivity";

    private static final String KEY_TITRE = "titre";
    private static final String KEY_DEBUT = "debut";
    private static final String KEY_FIN = "fin";
    private static final String KEY_LIEU = "lieu";
    private static final String KEY_LOCALISATION = "localisation";
    private static final String KEY_RAYON = "rayon";
    private static final String KEY_DESCRIPTION = "description";

    private TextView txtTitre;
    private TextView txtDebut;
    private TextView txtFin;
    private TextView txtLieu;
    private TextView txtLatitude;
    private TextView txtLongitude;
    private TextView txtRayon;
    private TextView txtDescription;

    private Button btnEmployeeMissionBtn;

    MediaPlayer off;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference noteRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_mission);

        //Change la fleche retour par une croix et le nom
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle(getString(R.string.showMission));

        txtTitre = findViewById(R.id.titre);
        txtDebut = findViewById(R.id.debut);
        txtFin = findViewById(R.id.fin);
        txtLieu = findViewById(R.id.lieu);
        txtLatitude = findViewById(R.id.latitude);
        txtLongitude = findViewById(R.id.longitude);
        txtRayon = findViewById(R.id.rayon);
        txtDescription = findViewById(R.id.description);

        btnEmployeeMissionBtn = findViewById(R.id.employeeMissionBtn);

        btnEmployeeMissionBtn.setOnClickListener(this);

        off = MediaPlayer.create(this, R.raw.sound_off);

        //récupération du chemin du document concerné envoyer par putExtra() depuis le MainMenuActivity
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if(b!=null){
            String docRef = (String) b.get("DOCUMENT_PATH");
             noteRef = db.document(docRef);
        }
        loadNote();
    }

    // Récupération des données du document sélectionné sur la base de données pour afficher par la suite
    public void loadNote() {
        noteRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String titre = documentSnapshot.getString(KEY_TITRE);
                            Date debut = documentSnapshot.getDate(KEY_DEBUT, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE);
                            Date fin = documentSnapshot.getDate(KEY_FIN);
                            String lieu = documentSnapshot.getString(KEY_LIEU);
                            GeoPoint localisation = documentSnapshot.getGeoPoint(KEY_LOCALISATION);
                            Object rayon = documentSnapshot.get(KEY_RAYON);
                            String description = documentSnapshot.getString(KEY_DESCRIPTION);

                            txtTitre.setText(titre);
                            txtDebut.setText(new SimpleDateFormat("EEE, dd-MM-yy  HH:mm aaa", Locale.getDefault()).format(debut));
                            txtFin.setText(new SimpleDateFormat("EEE, dd-MM-yy  HH:mm aaa", Locale.getDefault()).format(fin));
                            txtLieu.setText(lieu);
                            txtLatitude.setText(""+localisation.getLatitude());
                            txtLongitude.setText(""+localisation.getLongitude());
                            txtRayon.setText(rayon.toString());
                            txtDescription.setText(description);
                        } else {
                            Toast.makeText(CompleteMissionActivity.this, getString(R.string.doc_dont_exit), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CompleteMissionActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });
    }

    // Fonction qui identifie sur quel bouton l'utilisateur a cliqué
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.employeeMissionBtn){
            String path = noteRef.getId();
            //System.out.println(noteRef.collection(path).document().getId());
            Intent startEmployeeInMissionActivity = new Intent(CompleteMissionActivity.this, EmployeesInMissionActivity.class);
            startEmployeeInMissionActivity.putExtra("DOCUMENT_PATH", path);
            startActivity(startEmployeeInMissionActivity);
        }
    }

    // Fermeture de l'activité quand on clic sur la croix
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                mediaPlayer(off);
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Jouer un son
    public void mediaPlayer(MediaPlayer m) {
        m.start();
    }
}
