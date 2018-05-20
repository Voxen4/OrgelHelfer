package de.ostfalia.mobile.orgelhelfer;

import android.app.ListActivity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

public class Setup extends AppCompatActivity {

    private EditText et_dateipfad;
    private ImageButton btn_dateipfad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        et_dateipfad = (findViewById(R.id.dateipfad));
        btn_dateipfad = (findViewById(R.id.ib_dateipfad));





    }

}
