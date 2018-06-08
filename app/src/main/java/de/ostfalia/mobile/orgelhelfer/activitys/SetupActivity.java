package de.ostfalia.mobile.orgelhelfer.activitys;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;

import de.ostfalia.mobile.orgelhelfer.BuildConfig;
import de.ostfalia.mobile.orgelhelfer.R;

/**
 * Activity to handle different settings and see information about the application
 */

public class SetupActivity extends ListActivity {

    public static final String DATEIPFAD_KEY = "DATEIPFAD_KEY";
    public static final String PLAYLIST_DELAY_KEY = "PLAYLIST_DELAY_KEY";
    private static final String LOG_TAG = SetupActivity.class.getSimpleName();
    private TextView et_dateipfad;
    private ImageButton btn_dateipfad;
    private EditText verzoegerung_playlist;
    private TextView versionText;
    private TextView mailContact;

    /**
     * Shows different information about the application.
     * Sets the delaytime of the playlist with SharedPreferences
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        et_dateipfad = (findViewById(R.id.dateipfad));
        btn_dateipfad = (findViewById(R.id.ib_dateipfad));
        verzoegerung_playlist = findViewById(R.id.verzoegerung_playlist);
        et_dateipfad.setText(preferences.getString(DATEIPFAD_KEY, Environment.getExternalStorageDirectory().getPath() + "/OrgelHelfer"));
        if (preferences.getInt(PLAYLIST_DELAY_KEY, -1) != -1) {
            verzoegerung_playlist.setText(String.valueOf(preferences.getInt(PLAYLIST_DELAY_KEY, 200)));
        }
        verzoegerung_playlist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (s.toString().length() > 0) {
                    preferences.edit().putInt(PLAYLIST_DELAY_KEY, Integer.parseInt(s.toString())).apply();
                }
            }
        });

        btn_dateipfad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPathPickerDialog(v);
            }
        });
        versionText = findViewById(R.id.versionAnzeige);
        versionText.setText(BuildConfig.VERSION_NAME);
        mailContact = findViewById(R.id.textView5);
        mailContact.setText(getString(R.string.mail_contact));




    }

    /**
     * shows the FilePickerDialog to choose the destination folder to save the json objects
     * @param view
     */
    private void showPathPickerDialog(View view) {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(Environment.getExternalStorageDirectory().getPath());
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"json"};
        FilePickerDialog dialog = new FilePickerDialog(this, properties);
        dialog.setTitle(getString(R.string.path_select));
        dialog.show();
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                File file = new File(files[0]);
                if (file.exists()) {
                    et_dateipfad.setText(file.getPath());
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    preferences.edit().putString(DATEIPFAD_KEY, file.getPath()).apply();
                }
            }
        });


    }
    @Override
    public void onPause() {
        super.onPause();
    }

}
