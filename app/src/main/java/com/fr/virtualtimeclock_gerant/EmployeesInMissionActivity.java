package com.fr.virtualtimeclock_gerant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class EmployeesInMissionActivity extends BaseActivity {

    private static final String TAG = "EmployeeInThisMission";
    FirebaseFirestore db;
    private CollectionReference employeeInMissionRef;
    RecyclerView mRecyclerView;

    ArrayList<CompleteEmployeeInMission> completeEmployeeInMissionArrayList;

    EmployeesInMissionAdapter adapter;
    Button updateButton;

    String missionRef;
    String pointageId;

    MediaPlayer off;
    MediaPlayer on;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employees_in_mission);

        //Change la fleche retour par une croix et le nom
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle(getString(R.string.showEmployeeInMission));

        off = MediaPlayer.create(this, R.raw.sound_off);
        on = MediaPlayer.create(this, R.raw.sound_on);

        completeEmployeeInMissionArrayList = new ArrayList<>();

        mRecyclerView = findViewById(R.id.mRecyclerView_EiM);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        // Récupération le l'id de la mission envoyer depuis l'activité précédante
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if(b!=null){
            String docRef = (String) b.get("DOCUMENT_PATH");
            employeeInMissionRef = db.collection("pointage").document(docRef).collection("pointageMission");
            missionRef = docRef;
        }

        loadDataFromFirebase();
        setUpUpdateButton();
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

    // Bouton pour actualiser les employés pointés
    private  void setUpUpdateButton(){
        updateButton = findViewById(R.id.mUpdate_EiM);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer(on);
                loadDataFromFirebase();
            }
        });
    }

    // Chargement des employés ayant pointés dans la mission actuellement sélection
    //      et vérification de si l'utilisateur pointé existe toujours pour ensuite le supprimé de la liste
    public void loadDataFromFirebase() {
        employeeInMissionRef
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        completeEmployeeInMissionArrayList.clear();     //vider la liste pour éviter des duplication lors de l'actualisation des données
                        showProgressDialog();
                        for (final QueryDocumentSnapshot document : task.getResult()) {


                            pointageId= null;

                            // Lecture dans la collection utilisateur ------------------------------------------------------------------------------------
                            DocumentReference employeeRef = db.collection("utilisateurs").document(document.getId());


                            final Employee employee = new Employee();
                            CompleteEmployeeInMission ceim = new CompleteEmployeeInMission();
                            final CompleteEmployeeInMission finalCeim = ceim;

                            employeeRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    DocumentSnapshot doc = task.getResult();
                                    if (doc.exists()) {
                                        Log.d(TAG, "l'employé existe");
                                        Log.d(TAG, "Employé : "+doc.getId() + " => " + doc.getData());

                                        employee.setNom(doc.getString("nom"));
                                        employee.setPrenom(doc.getString("prenom"));
                                        finalCeim.setNom(employee.getNom());
                                        finalCeim.setPrenom(employee.getPrenom());
                                        finalCeim.setDate(document.getDate("date"));
                                        finalCeim.setEstPresent(document.getBoolean("estPresent"));
                                    } else {
                                        Log.d(TAG, "l'employé n'exite plus");
                                        Log.d("EMPLOYEE", doc.getId() + " => " + doc.getData());

                                        pointageId = doc.getId();

                                        finalCeim.setNom(getString(R.string.user_deleted));
                                        finalCeim.setDate(new Date());
                                        finalCeim.setEstPresent(false);
                                    }
                                }
                            }).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    hideProgressDialog();
                                    Log.d(TAG, "Employee Add in mission : success");
                                    Log.d(TAG, "missionRef avant envoi : "+missionRef);
                                    Log.d(TAG, "pointageId avant envoi : "+pointageId);
                                        adapter = new EmployeesInMissionAdapter(EmployeesInMissionActivity.this, completeEmployeeInMissionArrayList, missionRef, pointageId );
                                        mRecyclerView.setAdapter(adapter);
                                }
                            });
                            //Données utilisateurs récupérées --------------------------------------------------------------------------

                            Log.d(TAG , "Pointage Employé : "+document.getId() + " => " + document.getData());
                            completeEmployeeInMissionArrayList.add(ceim);
                            Log.d(TAG, "completeEmployeeInMissionActivity : " + completeEmployeeInMissionArrayList);

                        }
                    } else {
                        Log.d("TAG", "Error getting employee in missions documents: ", task.getException());
                    }
                }
            });
    }

}