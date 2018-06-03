package de.ostfalia.mobile.orgelhelfer.db;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import de.ostfalia.mobile.orgelhelfer.R;

public class Playlist_Tracks extends AppCompatActivity {

    TextView tx_name;
    ImageView trackHinzufuegen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist__tracks);

        tx_name = findViewById(R.id.playlistName);
        tx_name.setText(getIntent().getStringExtra("playlistName"));
        tx_name.setTextSize(25);
        tx_name.setTextAppearance(R.style.fontForNotificationLandingPage);

        trackHinzufuegen = findViewById(R.id.trackHinzufügen);
        trackHinzufuegen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRecordings(v);
            }
        });

    }


    public void loadRecordings(View view) {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(Environment.getExternalStorageDirectory().getPath() + "/OrgelHelfer");
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"json"};
        FilePickerDialog dialog = new FilePickerDialog(this, properties);
        dialog.setTitle("Datei auswählen");
        dialog.show();
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                File file = new File(files[0]);
                JSONObject jsonObject = null;
                if (file.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        StringBuilder jsonString = new StringBuilder();
                        while (reader.ready()) {
                            String line = reader.readLine();
                            jsonString.append(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


}
