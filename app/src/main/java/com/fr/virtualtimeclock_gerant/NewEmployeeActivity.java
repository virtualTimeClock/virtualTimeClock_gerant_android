package com.fr.virtualtimeclock_gerant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class NewEmployeeActivity extends AppCompatActivity {

    private static final String TAG = "NewEmployeeActivity";
    private boolean DIALOG_DELETE_DATA_ALREADY_RUN = false;

    private static final String KEY_NAME = "nom";
    private static final String KEY_FIRSTNAME = "prenom";
    private static final String KEY_BIRTHDAY = "dateNaissance";
    private static final String KEY_LEADER = "isLeader";

    private EditText editTextName;
    private EditText editTextFirstname;
    private EditText editTextEmail;
    private EditText editTextEmailVerif;

    private Button selectBirthday;
    private TextView textBirthday;

    private FirebaseAuth mAuth1;
    private FirebaseAuth mAuth2;

    private String currentDate;

    private SensorManager sm;
    private float acelVal;  //valeur actuel de l'acceleration et gravité
    private float acelLast; //dernière valeuur de l'acceleration et gravité
    private float shake;    //différence de la veleur de l'acceleration et la gravité

    DatePickerDialog datePickerDialogBirthday;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_employee);

        //Change la fleche retour par une croix et le nom
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle(getString(R.string.addEmployee));

        editTextName = findViewById(R.id.edit_text_name);
        editTextFirstname = findViewById(R.id.edit_text_firstname);
        editTextEmail = findViewById(R.id.edit_text_email);
        editTextEmailVerif = findViewById(R.id.edit_text_email_verification);

        selectBirthday = findViewById(R.id.btnBirthday);
        textBirthday = findViewById(R.id.textviewBirthday);

        // Maintenir l'utilisateur connecté sans qu'il change de compte à cause de la
        //      connexion automatique lors de la création d'une autre utilisateur
        mAuth1 = FirebaseAuth.getInstance();
        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setDatabaseUrl("https://virtual-time-clock-d7449.firebaseio.com/")
                .setApiKey("AIzaSyC0R8AIr8-6vgw3ODTjpjjppnFvbM7B4kM")
                .setApplicationId("virtual-time-clock-d7449").build();
        try { FirebaseApp myApp = FirebaseApp.initializeApp(getApplicationContext(), firebaseOptions, "AnyAppName");
            mAuth2 = FirebaseAuth.getInstance(myApp);
        } catch (IllegalStateException e){
            mAuth2 = FirebaseAuth.getInstance(FirebaseApp.getInstance("AnyAppName"));
        }

        // Acceleromètre
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(sensorListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        acelVal = SensorManager.GRAVITY_EARTH;
        acelLast = SensorManager.GRAVITY_EARTH;
        shake = 0.00f;

        selectBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                datePickerDialogBirthday = new DatePickerDialog(NewEmployeeActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                textBirthday.setText((month + 1) + "/" + day + "/" + year);
                            }
                        },year,month,dayOfMonth);
                currentDate = (month + 1) + "/" + dayOfMonth + "/" + year;

                datePickerDialogBirthday.show();
            }
        });
    }

    // Fonction qui crée et envoie les données saisitent sur la base de données
    public void createEmployee() {
        String name = editTextName.getText().toString();
        String firstname = editTextFirstname.getText().toString();
        String birthday = textBirthday.getText().toString();
        String leader = "false";

        String email  = editTextEmail.getText().toString();
        String email_verif = editTextEmailVerif.getText().toString();

        // Demande à saisir tous les champs avant d'envoyer
        boolean verif_date = getString(R.string.dateFormat).equals(birthday);
        if(verif_date || name.trim().isEmpty() || firstname.trim().isEmpty() || birthday.trim().isEmpty()
                || email.trim().isEmpty() || email_verif.trim().isEmpty()){
            Toast.makeText(this, getString(R.string.emptyField), Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérifie si la date de naissance est plus petite que la date actuelle
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            Date birth = sdf.parse(birthday);
            Date current = sdf.parse(currentDate);
            if(!(birth.compareTo(current)<0)){
                Toast.makeText(this, getString(R.string.error_birthday), Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Vérification que les emails sont identiques
        boolean same_email = email.equals(email_verif);
        if (!same_email){
            Toast.makeText(this, getString(R.string.email_different), Toast.LENGTH_SHORT).show();
            return;
        }

        // Map qui contient les clés (nom des clés dans la base de données) et les valeurs(valeurs des clés)
        //    à qu'on ajoute chaque donnée qui ont été saisie avant d'être envoyés
        Map<String, Object> employee = new HashMap<>();
        employee.put(KEY_NAME, name);
        employee.put(KEY_FIRSTNAME, firstname);
        employee.put(KEY_LEADER, Boolean.valueOf(leader));
        employee.put(KEY_BIRTHDAY, new Timestamp(new Date(birthday)));

        db.collection("utilisateurs").document().set(employee)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(NewEmployeeActivity.this,getString(R.string.creation_employee_send) , Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewEmployeeActivity.this,getString(R.string.creation_employee_fail), Toast.LENGTH_SHORT).show();
                Log.d(TAG, e.toString());
            }
        });

        // envoi d'un email pour que l'employé puisse réinitialiser le mdp
        mAuth1.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                        }
                    }
                });

        // Déconnexion de l'employé créé par l'utilisateur
        String password = generatPassword(8);
        mAuth2.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail : success");
                        mAuth2.signOut();
                    } else {
                        Log.w(TAG, "createUserWithEmail : failure", task.getException());
                        Toast.makeText(NewEmployeeActivity.this, getString(R.string.creation_employee_mail_fail),  Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    // Génération d'un mdp aléatoire
    private  String generatPassword(int length){
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for(int i=0; i<length;i++){
            char c = chars[random.nextInt(chars.length)];
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    // Menu qui contient le bouton pour quitter et sauvegarder
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.new_mission_menu, menu);
        return true;
    }

    // Execution de la fonction createEmployee lorsque l'on clique sur le bouton sauvegarder du menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_mission:
                createEmployee();
                return true;
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
                    AlertDialog.Builder alert = new AlertDialog.Builder(NewEmployeeActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(getString(R.string.suppression))
                            .setMessage(getString(R.string.suppression_msg))
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    DIALOG_DELETE_DATA_ALREADY_RUN = false;
                                    editTextName.setText("");
                                    editTextFirstname.setText("");
                                    textBirthday.setText(getString(R.string.dateFormat));
                                    editTextEmail.setText("");
                                    editTextEmailVerif.setText("");
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

    //Masque le clavier en cliquant ailleurs sur l'écran
    public void closeKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
