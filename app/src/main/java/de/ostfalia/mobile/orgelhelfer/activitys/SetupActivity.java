package de.ostfalia.mobile.orgelhelfer.activitys;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.IOException;

import de.ostfalia.mobile.orgelhelfer.MidiConnectionManager;
import de.ostfalia.mobile.orgelhelfer.R;


public class SetupActivity extends ListActivity {

    private static final String LOG_TAG = SetupActivity.class.getSimpleName();
    private EditText et_dateipfad;
    private ImageButton btn_dateipfad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        et_dateipfad = (findViewById(R.id.dateipfad));
        btn_dateipfad = (findViewById(R.id.ib_dateipfad));

        btn_dateipfad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
