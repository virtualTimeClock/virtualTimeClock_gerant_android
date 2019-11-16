package com.fr.virtualtimeclock_gerant;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

        findViewById(R.id.emailLogInButton).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    //Masque le clavier en cliquant ailleurs sur l'écran
    public void closeKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

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
                        FailLogInAlertDialog(getString(R.string.auth_failed));
                    }
                    hideProgressDialog();
                }
            });
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
                        leader = document.getData().get("isLeader");    //Récupération dans la base de données de la variable indiquant l'état leader
                        if(leader.equals(true)){    //Si c'est un leader, on lance l'activité principale
                            Log.d(TAG, "isLeader: " + leader);
                            Toast.makeText(AuthActivity.this,getString(R.string.its_a_leader_sucess_connection),
                                    Toast.LENGTH_SHORT).show();
                            startActivity( new Intent(AuthActivity.this, MainMenuActivity.class));
                        }else {     //Sinon il reste sur la page de connexion et reçoitun message lui informant qu'il na pas le droit de ce conneter
                            Log.d(TAG, "isLeader: " + leader);
                            mAuth.signOut();
                            Toast.makeText(AuthActivity.this,getString(R.string.its_not_a_leader),
                                    Toast.LENGTH_SHORT).show();
                            FailLogInAlertDialog(getString(R.string.its_not_a_leader));
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


    public void FailLogInAlertDialog(String errorMsg){
        new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle(getString(R.string.error))
                .setMessage(errorMsg)
                .setCancelable(true)
                .setPositiveButton(android.R.string.yes, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    //Fonction qui identifie sur quel bouton l'utilisateur a cliqué
    @Override
    public void onClick(View v) {
        int i = v.getId();
        //Bouton qui envoie l'email et le mdp saisite dans la fonction signIn pour essayer d'établir une connexion
        if (i == R.id.emailLogInButton) {
            if(!(mEmailField.getText().toString().trim().isEmpty() || mPasswordField.getText().toString().trim().isEmpty()) ) {
                signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
            }else{
                Toast.makeText(this, getString(R.string.emptyField), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
