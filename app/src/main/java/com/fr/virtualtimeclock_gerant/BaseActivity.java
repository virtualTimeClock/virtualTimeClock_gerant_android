package com.fr.virtualtimeclock_gerant;

import android.app.ProgressDialog;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    //Affiche une boite de chargement lors de la connexion de l'utilisateur
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }
    //Masquer la boite de chargement une fois la connexion de l'utilisateur faite ou non
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    //Masque la boite de chargement quand on clique ailleurs sur l'écran
    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

}