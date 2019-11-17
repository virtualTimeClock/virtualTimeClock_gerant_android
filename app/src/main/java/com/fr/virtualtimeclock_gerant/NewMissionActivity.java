package com.fr.virtualtimeclock_gerant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NewMissionActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "NewMissionActivity";

    private static final String KEY_TITLE = "titre";
    private static final String KEY_DATE_START = "debut";
    private static final String KEY_DATE_STOP = "fin";
    private static final String KEY_LOCATION = "lieu";
    private static final String KEY_RADIUS = "rayon";
    private static final String KEY_LOCALISATION = "localisation";
    private static final String KEY_DESCRIPTION = "description";

    private EditText editTextTitle;
    private EditText editTextLatitude;
    private EditText editTextLongitude;
    private EditText editTextRadius;
    private EditText editTextLocalisation;
    private EditText editTextDescription;

    private Button selectDateStart;
    private Button selectDateStop;
    private Button geolocalisationBtn;
    private TextView textDateStart;
    private TextView textDateStop;

    private Location location;
    private final int REQUEST_LOCATION = 200;
    private boolean DIALOG_ALREADY_RUN = false;
    private boolean DIALOG_DELETE_DATA_ALREADY_RUN = false;
    private boolean LOCALISATION_PRESSED = false;

    private SensorManager sm;
    private float acelVal;  //valeur actuel de l'acceleration et gravité
    private float acelLast; //dernière valeuur de l'acceleration et gravité
    private float shake;    //différence de la veleur de l'acceleration et la gravité

    DatePickerDialog datePickerDialogStart;
    DatePickerDialog datePickerDialogStop;

    MediaPlayer on;
    MediaPlayer off;
    MediaPlayer error;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_mission);

        //Change la fleche retour par une croix et le nom
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle(getString(R.string.addMission));

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextLatitude = findViewById(R.id.edit_text_latitude);
        editTextLongitude = findViewById(R.id.edit_text_longitude);
        editTextRadius = findViewById(R.id.edit_text_radius);
        editTextLocalisation = findViewById(R.id.edit_text_localisation);
        editTextDescription = findViewById(R.id.edit_text_description);

        selectDateStart = findViewById(R.id.btnDateStart);
        selectDateStop = findViewById(R.id.btnDateStop);
        geolocalisationBtn = findViewById(R.id.geolocalisation_btn);
        textDateStart = findViewById(R.id.textviewDateStart);
        textDateStop = findViewById(R.id.textviewDateStop);

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(sensorListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        acelVal = SensorManager.GRAVITY_EARTH;
        acelLast = SensorManager.GRAVITY_EARTH;
        shake = 0.00f;

        on = MediaPlayer.create(this, R.raw.sound_on);
        off = MediaPlayer.create(this, R.raw.sound_off);
        error = MediaPlayer.create(this, R.raw.error_sound);

        // Rendre les zones de textes non sélectionable
        editTextLatitude.setKeyListener(null);
        editTextLongitude.setKeyListener(null);

        // Button qui ouvre le calendrier pour choisir la date de début de mission
        selectDateStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mediaPlayer(on);
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                datePickerDialogStart = new DatePickerDialog(NewMissionActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                textDateStart.setText((month + 1) + "/" + day + "/" + year);
                            }
                        },year,month,dayOfMonth);

                datePickerDialogStart.show();
            }
        });

        // Button qui ouvre le calendrier pour choisir la date de fin de mission
        selectDateStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mediaPlayer(on);
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                datePickerDialogStop = new DatePickerDialog(NewMissionActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                textDateStop.setText((month + 1) + "/" + day + "/" + year);
                            }
                        },year,month,dayOfMonth);

                datePickerDialogStop.show();
            }
        });

        // Button qui récupère les données de localisation de la mission
        //  - seulement si la localisation est autorisée par l'utilisateur et activée sur le téléphone
        //  - si ce n'est pas le cas, demande l'activation des permissions et envoie une boîte de dialogue qui propose d'aller activer le GPS
        geolocalisationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationManager locationManager =  (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                String provider = LocationManager.GPS_PROVIDER;
                LOCALISATION_PRESSED = true;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(NewMissionActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                    } else {
                        locationManager.requestLocationUpdates(provider, 100, 2, NewMissionActivity.this);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
                    }
                }
                if (locationManager.isProviderEnabled(provider)) {
                    if (location != null) {
                        editTextLatitude.setText(String.valueOf(location.getLatitude()));
                        editTextLongitude.setText(String.valueOf(location.getLongitude()));
                    }
                } else {
                    if(!DIALOG_ALREADY_RUN) {
                        showGPSDisabledAlertToUser();
                        DIALOG_ALREADY_RUN = true;
                    }
                }
            }
        });
    }

    //Jouer un son
    public void mediaPlayer(MediaPlayer m) {
        m.start();
    }

    // Boitede dialogue qui propose l'activation du GPS en allant dans les Paramètres
    private void showGPSDisabledAlertToUser() {
        mediaPlayer(error);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        alertDialogBuilder.setMessage(getString(R.string.gps_disabled))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.gps_parameter), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DIALOG_ALREADY_RUN = false;
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                DIALOG_ALREADY_RUN = false;
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        try {       //try/catch ajouter sinon crash quand on désactive la localisation en étant sur l'activité
            alert.show();
        } catch (WindowManager.BadTokenException e) {
            Toast.makeText(this, getString(R.string.lost_localisation), Toast.LENGTH_SHORT).show();
        }

    }

    //Masque le clavier en cliquant ailleurs sur l'écran
    public void closeKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
}

    // Menu qui contient le bouton pour quitter et sauvegarder
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.new_mission_menu, menu);
        return true;
    }

    // Execution de la fonction createMission lorsque l'on clique sur le bouton sauvegarder du menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_mission:
                createMission();
                return true;
            case android.R.id.home:
                mediaPlayer(off);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Fonction qui crée et envoie les données saisitent sur la base de données
    public void createMission() {
        String title = editTextTitle.getText().toString();
        String start = textDateStart.getText().toString();
        String stop  = textDateStop.getText().toString();
        String latitude = editTextLatitude.getText().toString();
        String longitude = editTextLongitude.getText().toString();
        String radius = editTextRadius.getText().toString();
        String location = editTextLocalisation.getText().toString();
        String description = editTextDescription.getText().toString();

        // Demande à saisir tous les champs avant d'envoyer
        boolean verif_dateStart = getString(R.string.dateFormat).equals(start);
        boolean verif_dateStop = getString(R.string.dateFormat).equals(stop);
        if(verif_dateStart || verif_dateStop || title.trim().isEmpty() || start.trim().isEmpty() || start.trim().isEmpty()
                || latitude.trim().isEmpty() || longitude.trim().isEmpty() || radius.trim().isEmpty()
                || location.trim().isEmpty() || description.trim().isEmpty()){
            mediaPlayer(error);
            Toast.makeText(this, getString(R.string.emptyField), Toast.LENGTH_SHORT).show();
            return;
        }
        // Vérifie si la date de début est plus petite que la date de fin
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            Date date1 = sdf.parse(start);
            Date date2 = sdf.parse(stop);
            if(!(date1.compareTo(date2)<0)){
                mediaPlayer(error);
                Toast.makeText(this, getString(R.string.error_date), Toast.LENGTH_SHORT).show();
                return;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Map qui contient les clés (nom des clés dans la base de données) et les valeurs(valeurs des clés)
        //    à qu'on ajoute chaque donnée qui ont été saisie avant d'être envoyés
        Map<String, Object> mission = new HashMap<>();
        mission.put(KEY_TITLE, title);
        mission.put(KEY_DATE_START, new Timestamp(new Date(start)));
        mission.put(KEY_DATE_STOP, new Timestamp(new Date(stop)));
        mission.put(KEY_LOCATION, location);
        mission.put(KEY_RADIUS, Integer.valueOf(radius));
        mission.put(KEY_LOCALISATION,new GeoPoint(Double.parseDouble(latitude),Double.parseDouble(longitude)));
        mission.put(KEY_DESCRIPTION, description);

        db.collection("missions").document().set(mission)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mediaPlayer(on);
                        Toast.makeText(NewMissionActivity.this,getString(R.string.creation_mission_send) , Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mediaPlayer(error);
                Toast.makeText(NewMissionActivity.this,getString(R.string.creation_mission_fail), Toast.LENGTH_SHORT).show();
                Log.d(TAG, e.toString());
            }
        });
    }

    // Fonction de secousse pour supprimer toutes les données saisies seulement si l'utilisteur valide un boite de dialogue de confirmation
    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            acelLast = acelVal;
            acelVal = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = acelVal - acelLast;
            shake = shake * 0.9f + delta;

            if(shake > 13){
                if(!DIALOG_DELETE_DATA_ALREADY_RUN) {
                    mediaPlayer(off);
                    AlertDialog.Builder alert = new AlertDialog.Builder(NewMissionActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getString(R.string.suppression))
                        .setMessage(getString(R.string.suppression_msg))
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DIALOG_DELETE_DATA_ALREADY_RUN = false;
                                editTextTitle.setText("");
                                textDateStart.setText(getString(R.string.dateFormat));
                                textDateStop.setText(getString(R.string.dateFormat));
                                editTextLatitude.setText("");
                                editTextLongitude.setText("");
                                editTextRadius.setText("");
                                editTextLocalisation.setText("");
                                editTextDescription.setText("");
                                Toast.makeText(getApplicationContext(),getString(R.string.suppression_data_success),Toast.LENGTH_LONG).show();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DIALOG_DELETE_DATA_ALREADY_RUN = false;
                                Toast.makeText(getApplicationContext(),getString(R.string.suppression_data_cancel),Toast.LENGTH_LONG).show();
                            }
                        });
                    AlertDialog alerts = alert.create();
                    try {       // try/catch ajouter sinon crash quand on quitte l'activité
                        alerts.show();
                    } catch (WindowManager.BadTokenException e) {
                        Log.d(TAG, "onSensorChanged: ", e);
                    }
                }
                DIALOG_DELETE_DATA_ALREADY_RUN = true;
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { /*Pas utilisé*/ }
    };

    // Affiche les coordonnées en temps réel de l'utilisateur
    @Override
    public void onLocationChanged(Location location) {
        if(LOCALISATION_PRESSED){
            mediaPlayer(on);
            editTextLatitude.setText(String.valueOf(location.getLatitude()));
            editTextLongitude.setText(String.valueOf(location.getLongitude()));
            LOCALISATION_PRESSED = false;
        }
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { /*Pas utilisé*/ }
    @Override
    public void onProviderEnabled(String provider) { /*Pas utilisé */ }
    //Détection quand la localisation est désactivé affiche la boite de dialogue pour la réactiver
    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            if(!DIALOG_ALREADY_RUN) {
                showGPSDisabledAlertToUser();
                DIALOG_ALREADY_RUN = true;
            }
        }
    }
}
