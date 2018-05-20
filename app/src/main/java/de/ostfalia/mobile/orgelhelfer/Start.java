package de.ostfalia.mobile.orgelhelfer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

public class Start extends AppCompatActivity {

    private CardView aufnahme, abspielen, genre, einstellungen, playlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        aufnahme = (CardView) findViewById(R.id.aufnahme);
        abspielen = (CardView) findViewById(R.id.Abspielen);
        genre = (CardView) findViewById(R.id.Genre);
        einstellungen = (CardView) findViewById(R.id.Einstellungen);
        playlist = (CardView) findViewById(R.id.Playlist);

        aufnahme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), BaseActivity.class));
            }
        });

        einstellungen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), Setup.class));
            }
        });

        playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), Playlist.class));
            }
        });
    }
}
