package com.fr.virtualtimeclock_gerant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MissionActivity extends AppCompatActivity {

    private static final String TAG = "MissionActivity";

    private static final String KEY_TITLE = "titre";
    private static final String KEY_DATE_START = "debut";
    private static final String KEY_DATE_STOP = "fin";
    private static final String KEY_LOCATION = "lieu";
    private static final String KEY_RADIUS = "rayon";
    private static final String KEY_LOCALISATION = "localisation";
    private static final String KEY_DESCRIPTION = "description";

    private EditText editTextTitle;
    private EditText editTextStart;
    private EditText editTextStop;
    private EditText editTextLatitude;
    private EditText editTextLongitude;
    private EditText editTextRadius;
    private EditText editTextLocalisation;
    private EditText editTextDescription;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextStart = findViewById(R.id.edit_text_start);
        editTextStop = findViewById(R.id.edit_text_stop);
        editTextLatitude = findViewById(R.id.edit_text_latitude);
        editTextLongitude = findViewById(R.id.edit_text_longitude);
        editTextRadius = findViewById(R.id.edit_text_radius);
        editTextLocalisation = findViewById(R.id.edit_text_localisation);
        editTextDescription = findViewById(R.id.edit_text_description);
    }

    //Fonction éxécuter pour envoyer les données saisitent des zones de textes sur la base de données
    public void createMission(View v) {
        String title = editTextTitle.getText().toString();
        String start = editTextStart.getText().toString();
        String stop  = editTextStop.getText().toString();
        String latitude = editTextLatitude.getText().toString();
        String longitude = editTextLongitude.getText().toString();
        String radius = editTextRadius.getText().toString();
        String location = editTextLocalisation.getText().toString();
        String description = editTextDescription.getText().toString();

        //Ajout des missions dans un collection de type Map<K,V>
        // - V : le nom de la clé de la base de données
        // - K : la données saisie convertie au format de la base de données
        Map<String, Object> mission = new HashMap<>();
        mission.put(KEY_TITLE, title);
        mission.put(KEY_DATE_START, new Timestamp(new Date(start)));
        mission.put(KEY_DATE_STOP, new Timestamp(new Date(stop)));
        mission.put(KEY_LOCATION, location);
        mission.put(KEY_RADIUS, Integer.valueOf(radius));
        mission.put(KEY_LOCALISATION,new GeoPoint(Double.parseDouble(latitude),Double.parseDouble(longitude)));
        mission.put(KEY_DESCRIPTION, description);

        //Envoi des données sur la base de données
        db.collection("missions").document().set(mission)
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MissionActivity.this,getString(R.string.creation_mission_send) , Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MissionActivity.this,getString(R.string.creation_mission_fail), Toast.LENGTH_SHORT).show();
                Log.d(TAG, e.toString());
            }
        });
    }
}
