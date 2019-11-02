package com.fr.virtualtimeclock_gerant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "EmailPassword";


    private TextView mStatusTextView;
    private EditText mEmailField;
    private EditText mPasswordField;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    Object leader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mEmailField = findViewById(R.id.fieldEmail);
        mPasswordField = findViewById(R.id.fieldPassword);
        mStatusTextView = findViewById(R.id.state);

        findViewById(R.id.emailSignInButton).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }
    /*@Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser !=null){
            getItems(currentUser.getUid());
        }
    }*/

    //Fonction exécuter lors de la connexion de l'utilisateur
    private void signIn(String email, String password) {
        Log.d(TAG, "sign In: " + email);

        //Lance la boite de chargement
        showProgressDialog();

        // Début de la connexion via email et mdp
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Connexion réussi, mise à jour des information de l'utilisateur
                        Log.d(TAG, "signInWithEmail : success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        getItems(user.getUid());
                    } else {
                        // Connexion échouer
                        Log.w(TAG, "signInWithEmail : failure", task.getException());
                        Toast.makeText(AuthActivity.this,getString(R.string.auth_failed),
                                Toast.LENGTH_SHORT).show();
                    }

                    // si la connexion échoue, affiche à l'utilisateur que la connexion à échouer puis masque la boite de chargement
                    if (!task.isSuccessful()) {
                        mStatusTextView.setText(R.string.auth_failed);
                    }
                    hideProgressDialog();
                }
            });
    }

    //Fonction éxécuter pour déconnecter
    private void signOut() {
        mAuth.signOut();
    }

    //Fonction qui récupère l'information qui précise si l'utilisateur est un Employeur ou non
    private void getItems(String id) {
        DocumentReference docRef = db.collection("utilisateurs").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        //Récupération dans la base de données de la variable indiquant l'état leader
                        leader = document.getData().get("isLeader");
                        mStatusTextView.setText(getString(R.string.its_a_leader_sucess_connection));
                        //Si c'est un leader, on lance l'activité principale
                        if(leader.equals(true)){
                            Log.d(TAG, "isLeader: " + leader);
                            Toast.makeText(AuthActivity.this,getString(R.string.its_a_leader_sucess_connection),
                                    Toast.LENGTH_SHORT).show();
                            startActivity( new Intent(AuthActivity.this, MainMenuActivity.class));
                        //Sinon il reste sur la page de connexion et reçoitun message lui informant qu'il na pas le droit de ce conneter
                        }else {
                            Log.d(TAG, "isLeader: " + leader);
                            mStatusTextView.setText(getString(R.string.its_not_a_leader));
                            mAuth.signOut();
                            Toast.makeText(AuthActivity.this,getString(R.string.its_not_a_leader),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }

            }
        });
    }

    //Fonction qui identifie sur quel bouton l'utilisateur a cliqué
    @Override
    public void onClick(View v) {
        int i = v.getId();
        //Bouton qui envoie l'email et le mdp saisite dans la fonction signIn pour essayer d'établir une connexion
        if (i == R.id.emailSignInButton) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }
}
