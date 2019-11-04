package com.fr.virtualtimeclock_gerant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.darwindeveloper.horizontalscrollmenulibrary.custom_views.HorizontalScrollMenuView;
import com.darwindeveloper.horizontalscrollmenulibrary.extras.MenuItem;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MainMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView userEmail;
    private Button userLogout;

    private HorizontalScrollMenuView menu;
    private FloatingActionButton buttonAdNote;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser =  mAuth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference notebookRef = db.collection("missions");

    private MissionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        menu = findViewById(R.id.menu);
        userEmail = findViewById(R.id.email);
        userLogout = findViewById(R.id.btnSignOut);
        buttonAdNote = findViewById(R.id.button_add_mission);

        userLogout.setOnClickListener(this);
        buttonAdNote.setOnClickListener(this);

        userEmail.setText(currentUser.getEmail());

        initMenu();

        setUpRecyclerView();

    }



    private void setUpRecyclerView() {
        Query query = notebookRef.orderBy("debut", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Mission> options = new FirestoreRecyclerOptions.Builder<Mission>()
                .setQuery(query, Mission.class)
                .build();

        adapter = new MissionAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Pour la suppression des mission on choisi de ne pas faire de drag and drop
        //   et de faire supprimer la mission par la droite ou la gauche lors du glissement
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            //Pour du drag and drop (inutiliser dans notre cas)
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            //Pour des mouvements de glissements
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);

        //Détecte le clic sur la mission
        adapter.setOnClickListener(new MissionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Mission mission = documentSnapshot.toObject(Mission.class);
                String id = documentSnapshot.getId();
                String path = documentSnapshot.getReference().getPath();
                Toast.makeText(MainMenuActivity.this, "Position: " + position+ " ID: "+id,
                        Toast.LENGTH_SHORT).show();
                //startActivity();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void initMenu() {
        menu.addItem("Profile", R.drawable.ic_profile);
        menu.addItem("Employee", R.drawable.ic_employee);
        menu.addItem("Mission",R.drawable.ic_mission_selected,true);
        menu.addItem("Logout", R.drawable.ic_logout);

        menu.setOnHSMenuClickListener(new HorizontalScrollMenuView.OnHSMenuClickListener() {
            @Override
            public void onHSMClick(MenuItem menuItem, int position) {
                String mainMenu = menuItem.getText();
                if(menuItem.getText().equals("Profile")) mainMenu = "Profile";
                else if(menuItem.getText().equals("Employee")) mainMenu = "Employee";
                else if(menuItem.getText().equals("Mission")) mainMenu = "Mission";
                else if(menuItem.getText().equals("Logout"))mainMenu = "Logout";
                switch(mainMenu){
                    case "Profile":
                        Toast.makeText(MainMenuActivity.this, ""+menuItem.getText(),
                                Toast.LENGTH_SHORT).show();

                        menu.setItemSelected(0);
                        menu.editItem(0,"Profile",R.drawable.ic_profile_selected,false,0);
                        menu.editItem(1,"Employee",R.drawable.ic_employee,false,0);
                        menu.editItem(2,"Mission",R.drawable.ic_mission,false,0);

                        findViewById(R.id.layout_profile).setVisibility(View.VISIBLE);
                        findViewById(R.id.layout_employee).setVisibility(View.GONE);
                        findViewById(R.id.layout_mission).setVisibility(View.GONE);
                        break;
                    case "Employee":
                        menu.setItemSelected(1);
                        Toast.makeText(MainMenuActivity.this, ""+menuItem.getText(),
                                Toast.LENGTH_SHORT).show();

                        menu.editItem(0,"Profile",R.drawable.ic_profile,false,0);
                        menu.editItem(1,"Employee",R.drawable.ic_employee_selected,false,0);
                        menu.editItem(2,"Mission",R.drawable.ic_mission,false,0);

                        findViewById(R.id.layout_profile).setVisibility(View.GONE);
                        findViewById(R.id.layout_employee).setVisibility(View.VISIBLE);
                        findViewById(R.id.layout_mission).setVisibility(View.GONE);
                        break;
                    case "Mission":
                        menu.setItemSelected(2);
                        Toast.makeText(MainMenuActivity.this, ""+menuItem.getText(),
                                Toast.LENGTH_SHORT).show();

                        menu.editItem(0,"Profile",R.drawable.ic_profile,false,0);
                        menu.editItem(1,"Employee",R.drawable.ic_employee,false,0);
                        menu.editItem(2,"Mission",R.drawable.ic_mission_selected,false,0);

                        findViewById(R.id.layout_profile).setVisibility(View.GONE);
                        findViewById(R.id.layout_employee).setVisibility(View.GONE);
                        findViewById(R.id.layout_mission).setVisibility(View.VISIBLE);
                        break;
                    case "Logout":
                        LogoutAlertDialog();
                        break;
                }

            }
        });
    }
    public void LogoutAlertDialog(){
        new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle(getString(R.string.sign_out))
                .setMessage(getString(R.string.msg_sign_out))
                .setCancelable(true)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainMenuActivity.this, "Disconnected",
                                Toast.LENGTH_SHORT).show();
                        userLogout();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private void userLogout() {
        mAuth.getInstance().signOut();
        Intent intent = new Intent(MainMenuActivity.this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    //Fonction qui identifie sur quel bouton l'utilisateur a cliqué
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btnSignOut) {
           userLogout();
        }else if(i == R.id.button_add_mission){
            startActivity(new Intent(MainMenuActivity.this, NewMissionActivity.class));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            LogoutAlertDialog();
        }
        return super.onKeyDown(keyCode, event);
    }
}
