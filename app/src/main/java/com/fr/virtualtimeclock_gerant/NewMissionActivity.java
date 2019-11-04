package com.fr.virtualtimeclock_gerant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewMissionActivity extends AppCompatActivity {

    private static final String TAG = "MissionActivity";

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
    private TextView textDateStart;
    private TextView textDateStop;

    DatePickerDialog datePickerDialogStart;
    DatePickerDialog datePickerDialogStop;

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
        textDateStart = findViewById(R.id.textviewDateStart);
        textDateStop = findViewById(R.id.textviewDateStop);

        selectDateStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
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

        selectDateStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.new_mission_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_mission:
                createMission();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }




    public void createMission() {
        String title = editTextTitle.getText().toString();
        String start = textDateStart.getText().toString();
        String stop  = textDateStop.getText().toString();
        String latitude = editTextLatitude.getText().toString();
        String longitude = editTextLongitude.getText().toString();
        String radius = editTextRadius.getText().toString();
        String location = editTextLocalisation.getText().toString();
        String description = editTextDescription.getText().toString();

        //Demande à saisir tous les champs avant d'envoyer
        if(title.trim().isEmpty() || start.trim().isEmpty() || start.trim().isEmpty()
                || latitude.trim().isEmpty() || longitude.trim().isEmpty() || radius.trim().isEmpty()
                || location.trim().isEmpty() || description.trim().isEmpty()){
            Toast.makeText(this, getString(R.string.emptyField), Toast.LENGTH_SHORT).show();
            return;
        }

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
                        Toast.makeText(NewMissionActivity.this,getString(R.string.creation_mission_send) , Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewMissionActivity.this,getString(R.string.creation_mission_fail), Toast.LENGTH_SHORT).show();
                Log.d(TAG, e.toString());
            }
        });
    }

    public void back(View v){
        finish();
    }
}
