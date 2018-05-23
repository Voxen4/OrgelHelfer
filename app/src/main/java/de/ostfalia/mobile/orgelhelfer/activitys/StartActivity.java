package de.ostfalia.mobile.orgelhelfer.activitys;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;

import de.ostfalia.mobile.orgelhelfer.R;

public class StartActivity extends AppCompatActivity {

    private static final String LOG_TAG = StartActivity.class.getSimpleName();
    private CardView aufnahme, abspielen, genre, einstellungen, playlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        aufnahme = findViewById(R.id.aufnahme);
        abspielen = findViewById(R.id.Abspielen);
        genre = findViewById(R.id.Genre);
        einstellungen = findViewById(R.id.Einstellungen);
        playlist = findViewById(R.id.Playlist);

        aufnahme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), BaseActivity.class));
            }
        });

        einstellungen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), SetupActivity.class));
            }
        });

        playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), PlaylistActivity.class));
            }
        });

        genre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), KategorieActivity.class));
            }
        });
        checkPermissions();
        if (!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            Log.d(LOG_TAG, "NO MIDI Support for this Device");
            noMidiSupportAlert(false).show();
        }

    }

    private void checkPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }

    }

    public AlertDialog noMidiSupportAlert(boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(cancelable);
        builder.setTitle(R.string.no_midi_service_title);
        builder.setMessage(R.string.no_midi_service);
        return builder.create();
    }


}
