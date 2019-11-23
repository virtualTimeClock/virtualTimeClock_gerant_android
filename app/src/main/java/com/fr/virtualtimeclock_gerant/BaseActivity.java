package com.fr.virtualtimeclock_gerant;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

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

    //Jouer un son
    public void mediaPlayer(MediaPlayer m) {
        m.setVolume(0.1f,0.1f);
        m.start();
    }

    //Masque le clavier en cliquant ailleurs sur l'écran
    public void closeKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

}