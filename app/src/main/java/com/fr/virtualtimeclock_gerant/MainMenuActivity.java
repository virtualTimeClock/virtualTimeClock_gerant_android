package com.fr.virtualtimeclock_gerant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.darwindeveloper.horizontalscrollmenulibrary.custom_views.HorizontalScrollMenuView;
import com.darwindeveloper.horizontalscrollmenulibrary.extras.MenuItem;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainMenuActivity extends BaseActivity implements View.OnClickListener {


    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int STORAGE_REQUEST_CODE = 2;
    private static final String TAG = "imageCreation";
    private final String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";

    private TextView userEmail;

    private Button buttonCapture;
    private ImageView companyPicture;

    private HorizontalScrollMenuView menu;
    private FloatingActionButton buttonAddMission;
    private FloatingActionButton buttonAddEmployee;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser =  mAuth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private FirebaseStorage storage;

    private CollectionReference missionsRef = db.collection("missions");
    private CollectionReference employeeRef = db.collection("utilisateurs");

    private MissionAdapter missionsAdapter;
    private EmployeeAdapter employeeAdapter;

    private File photoFile;
    private Uri filepath;

    MediaPlayer on;
    MediaPlayer off;
    MediaPlayer error;

    private String imgURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        on = MediaPlayer.create(this, R.raw.sound_on);
        off = MediaPlayer.create(this, R.raw.sound_off);
        error = MediaPlayer.create(this, R.raw.error_sound);

        menu = findViewById(R.id.menu);
        userEmail = findViewById(R.id.email);
        buttonAddMission = findViewById(R.id.button_add_mission);
        buttonAddEmployee = findViewById(R.id.button_add_employee);
        buttonCapture = findViewById(R.id.upload);
        companyPicture = findViewById(R.id.imageView);

        // Vérification des permissions pour la caméra et le stockage
        if(Build.VERSION.SDK_INT >= 23) requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);

        storage  = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        buttonAddMission.setOnClickListener(this);
        buttonCapture.setOnClickListener(this);
        buttonAddEmployee.setOnClickListener(this);

        userEmail.setText(currentUser.getEmail());

        initMenuBar();
        setUpRecyclerViewMissions();
        setUpRecyclerViewEmployee();
        loadProfilePicture();
    }



    // Création du menu avec toutes les onglets contenant les images et lors du clic sur un onglet
    //     il affiche le layout qui lui correspond en masquant les autres
    private void initMenuBar() {
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
                        mediaPlayer(on);
                        menu.setItemSelected(0);
                        menu.editItem(0,"Profile",R.drawable.ic_profile_selected,false,0);
                        menu.editItem(1,"Employee",R.drawable.ic_employee,false,0);
                        menu.editItem(2,"Mission",R.drawable.ic_mission,false,0);

                        findViewById(R.id.layout_profile).setVisibility(View.VISIBLE);
                        findViewById(R.id.layout_employee).setVisibility(View.GONE);
                        findViewById(R.id.layout_mission).setVisibility(View.GONE);
                        break;
                    case "Employee":
                        mediaPlayer(on);
                        menu.setItemSelected(1);
                        menu.editItem(0,"Profile",R.drawable.ic_profile,false,0);
                        menu.editItem(1,"Employee",R.drawable.ic_employee_selected,false,0);
                        menu.editItem(2,"Mission",R.drawable.ic_mission,false,0);

                        findViewById(R.id.layout_profile).setVisibility(View.GONE);
                        findViewById(R.id.layout_employee).setVisibility(View.VISIBLE);
                        findViewById(R.id.layout_mission).setVisibility(View.GONE);
                        break;
                    case "Mission":
                        mediaPlayer(on);
                        menu.setItemSelected(2);
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

    // Fonction qui identifie sur quel bouton l'utilisateur a cliqué
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.button_add_mission){
            mediaPlayer(on);
            startActivity(new Intent(MainMenuActivity.this, NewMissionActivity.class));
        }else if(i == R.id.upload){
            mediaPlayer(on);
            dispatchPictureTakerAction();
        }else if(i == R.id.button_add_employee){
            mediaPlayer(on);
            startActivity(new Intent(MainMenuActivity.this, NewEmployeeActivity.class));
        }
    }

    // Quand on clic sur le bouton retour du téléphone on ouvre la boite de dialog pour la déconnnexion
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            LogoutAlertDialog();
        }
        return super.onKeyDown(keyCode, event);
    }

    // ---------------------------------------------- Menu Profile : ------------------------------------ :

    // Récupère la photo de profile sur la base de données
    // - uri : url de téléchargement de la photo sur la base de données
    private void loadProfilePicture(){
        StorageReference profilePic = storageReference.child("Photos/profilePic").getParent().child("profilePic");
        profilePic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imgURL = String.valueOf(uri);
                Glide.with(getApplicationContext())
                        .load(imgURL)
                        .into(companyPicture);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(MainMenuActivity.this, getString(R.string.load_img_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Ouverture de l'appareil photo
    private void dispatchPictureTakerAction() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){ // S'assurer qu'il y a une application caméra pour lancer l'Intent
            //Création du fichier ou la photo va être sauvegarder
            photoFile = createPhotoFile();
            if(photoFile != null){
                filepath = FileProvider.getUriForFile(MainMenuActivity.this,"com.fr.virtualtimeclock_gerant.fileprovider",photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, filepath);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    // Création de la photos au format jpg avec comme nom la date ou la photo a été prise
    // Sauvegarde des photos dans le répertoire cache de l'application
    private File createPhotoFile(){
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_"+ name +"_VTC_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            Log.d(TAG,"ImageFile : "+e.toString());
        }
        return image;
    }

    // Envoi de l'image sur la base de données avec un affichage d'une barre de chargement
    private void uploadImage(Uri filepathCrop){
        if(filepathCrop != null){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference reference = storageReference.child("Photos/"+"profilePic");
            reference.putFile(filepathCrop)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(MainMenuActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                            loadProfilePicture();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainMenuActivity.this, "Upload Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }

    // Récupération de la photo prise pour ensuite éxecuter la fonction startCrop
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK  && requestCode == CAMERA_REQUEST_CODE ) {

            if (data != null) {
                filepath = data.getData();
            }
            startCrop(filepath);
        }else {
            Uri filepathCrop = null;
            if (data != null) {
                filepathCrop = UCrop.getOutput(data);
            }
            uploadImage(filepathCrop);
        }
    }

    // Fonction qui redimensionne la photo au format du cadre de la photo de profil
    private void startCrop(@NonNull Uri uri){
        String destinationFileName = SAMPLE_CROPPED_IMG_NAME;
        destinationFileName +=".jpg";

        UCrop uCrop = UCrop.of(uri,Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        uCrop.withAspectRatio(1,1);
        uCrop.withMaxResultSize(400,400);
        uCrop.withOptions(getCropOptions());
        uCrop.start(MainMenuActivity.this);
    }

    // Option nécessaire au redimensionnement
    private UCrop.Options getCropOptions(){
        UCrop.Options options = new UCrop.Options();

        options.setCompressionQuality(100);

        //CompressType
        //options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        //options.setCompressionFormat(Bitmap.CompressFormat.JPEG);

        //UI
        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(false);

        //Colors
        options.setStatusBarColor(getResources().getColor(R.color.colorOrangeButton));
        options.setToolbarColor(getResources().getColor(R.color.colorOrangeButton));

        options.setToolbarTitle("Crop Image");

        return options;
    }

    // ---------------------------------------------- Menu Employées : ---------------------------------- :

    // Affichage des employés ordonnées par nom
    // Suppression des employés avec des swipes latéraux
    private void setUpRecyclerViewEmployee() {
        Query query = employeeRef.orderBy("nom", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Employee> options = new FirestoreRecyclerOptions.Builder<Employee>()
                .setQuery(query, Employee.class)
                .build();

        employeeAdapter = new EmployeeAdapter(options);

        RecyclerView recyclerView_employee = findViewById(R.id.recycler_view_employee);
        recyclerView_employee.setHasFixedSize(true);
        recyclerView_employee.setLayoutManager(new LinearLayoutManager(this));
        recyclerView_employee.setAdapter(employeeAdapter);

        // Pour la suppression des employés on choisi de ne pas faire de drag and drop
        //   et de faire supprimer les employés par la droite ou la gauche lors du glissement
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            //Pour du drag and drop (inutiliser dans notre cas)
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            //Pour des mouvements de glissements
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                employeeAdapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView_employee);
    }

    // ---------------------------------------------- Menu Missions : ----------------------------------- :

    // Affichage des missions ordonnées par date de début
    // Suppression des missions avec des swipes latéraux
    // Ouverture des missions
    private void setUpRecyclerViewMissions() {
        Query query = missionsRef.orderBy("debut", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Mission> options = new FirestoreRecyclerOptions.Builder<Mission>()
                .setQuery(query, Mission.class)
                .build();

        missionsAdapter = new MissionAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_missions);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(missionsAdapter);

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
                missionsAdapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);

        //Détecte le clic sur la mission et envoi via putExtra du chemin pour pouvoir lire le document sélectionné
        missionsAdapter.setOnClickListener(new MissionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                String path = documentSnapshot.getReference().getPath();
                Intent startCompleteMenuActivity = new Intent(MainMenuActivity.this, CompleteMissionActivity.class);
                startCompleteMenuActivity.putExtra("DOCUMENT_PATH", path);
                startActivity(startCompleteMenuActivity);
            }
        });
    }

    // Début de l'écoute de la base de données
    @Override
    public void onStart() {
        super.onStart();
        missionsAdapter.startListening();
        employeeAdapter.startListening();
    }

    // fin de l'écoute de la base de données
    @Override
    public void onStop() {
        super.onStop();
        missionsAdapter.stopListening();
        employeeAdapter.stopListening();
    }

    // ---------------------------------------------- Menu Déconnexion : --------------------------------- :

    // Ouverture d'une boite de dialogue pour vérifier si l'utilisateur souhaite vraiment se déconnecter
    public void LogoutAlertDialog(){
        mediaPlayer(off);
        new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle(getString(R.string.log_out))
                .setMessage(getString(R.string.msg_sign_out))
                .setCancelable(true)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainMenuActivity.this, getString(R.string.disconnect),
                                Toast.LENGTH_SHORT).show();
                        userLogout();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Déconnexion de l'utilisateur en allant vers l'activité de connexion et en fermant toutes les autres activités lancées
    private void userLogout() {
        mAuth.getInstance().signOut();
        Intent intent = new Intent(MainMenuActivity.this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
