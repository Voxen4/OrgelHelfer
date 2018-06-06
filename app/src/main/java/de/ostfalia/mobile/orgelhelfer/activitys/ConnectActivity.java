package de.ostfalia.mobile.orgelhelfer.activitys;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.midi.MidiManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import de.ostfalia.mobile.orgelhelfer.MidiConnectionManager;
import de.ostfalia.mobile.orgelhelfer.MidiDataManager;
import de.ostfalia.mobile.orgelhelfer.MidiEventArrayAdapter;
import de.ostfalia.mobile.orgelhelfer.R;
import de.ostfalia.mobile.orgelhelfer.midi.CustomMidiDeviceInfo;
import de.ostfalia.mobile.orgelhelfer.model.MidiEvent;
import de.ostfalia.mobile.orgelhelfer.model.MidiRecording;
import de.ostfalia.mobile.orgelhelfer.services.Player;

import static android.widget.AdapterView.OnItemClickListener;
import static android.widget.AdapterView.OnItemSelectedListener;
import static de.ostfalia.mobile.orgelhelfer.activitys.SetupActivity.DATEIPFAD_KEY;

public class ConnectActivity extends BaseActivity implements MidiDataManager.OnMidiDataListener {

    private static final String LOG_TAG = ConnectActivity.class.getSimpleName();
    public static MidiRecording midiRecording;
    public ArrayList<MidiEvent> log = new ArrayList<>();
    private ListView listView;
    private Spinner spinner;
    private Button playButton;
    private boolean recording;
    private JSONObject jsonData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MidiManager midiManager = (MidiManager) getSystemService(MIDI_SERVICE);

        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            // do MIDI stuff
            // SetupActivity a menu to select an input source.
            MidiDataManager.getInstance().addOnMidiDataListener(this);
        }
        listView = findViewById(R.id.list);
        ArrayAdapter adapter = new MidiEventArrayAdapter(this, R.layout.simple_list_item_slim, log);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View view,
                                            int position, long id) {
                        MidiEvent event = (MidiEvent) listView.getItemAtPosition(position);
                        MidiDataManager.getInstance().sendEvent(event);
                    }
                }
        );

        spinner = findViewById(R.id.spinner);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        ArrayAdapter<CustomMidiDeviceInfo> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, devices);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG, "Item Selected " + position);
                CustomMidiDeviceInfo deviceInfo = (CustomMidiDeviceInfo) spinner.getItemAtPosition(position);
                MidiConnectionManager.getInstance().connectToDevice(deviceInfo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        playButton = findViewById(R.id.playButton);
        if (midiRecording != null) {
            playButton.setEnabled(true);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        MidiDataManager.getInstance().removeOnMidiDataListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        MidiDataManager.getInstance().addOnMidiDataListener(this);
        //stopService();
    }


    public void startRecording(View view) {
        final Button recordButton = (Button) view;
        if (recording) {
            recording = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recordButton.setText(R.string.start_recording);
                }
            });
            showSaveRecordingDialog();
        } else {
            jsonData = new JSONObject();
            try {
                jsonData.put(getString(R.string.json_recording), ((Long) System.currentTimeMillis()).toString());
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(LOG_TAG, "Error while switching recording " + e.toString());
            }
            recording = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recordButton.setText(R.string.stop_recording);
                }
            });
        }
        Log.d(LOG_TAG, "Switched recording state");
    }

    @Override
    public void onMidiData(final MidiEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, event.toString());
                log.add(0, event);
                ((MidiEventArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
                if (recording) {
                    try {
                        jsonData.put(getString(R.string.json_midievent) + jsonData.length(), event.toJsonObject());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d(LOG_TAG, getString(R.string.error_recording_add_note));
                    }
                }

            }
        });
    }


    @Override
    public void onDevicesChanged() {
        super.onDevicesChanged();
        Spinner spinner = findViewById(R.id.spinner);
        spinner.getAdapter();
        ArrayAdapter<CustomMidiDeviceInfo> arrayAdapter =
                new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, devices);
        //Had to add this listeners cause apparently spinner doesn't keep a reference to the original List.
        spinner.setAdapter(arrayAdapter);
    }

    public void writeMidiJsonExternal(String name) {
        Writer output = null;
        if (jsonData == null) {
            Log.d(LOG_TAG, "Json data is null");
            return;
        }
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "OrgelHelfer";
        File folder = new File(path + File.separator);
        if (!folder.exists()) {
            folder.mkdir();
        }
        File file = new File(folder + File.separator + name + ".json");
        try {
            output = new BufferedWriter(new FileWriter(file));
            output.write(jsonData.toString());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, getString(R.string.error_recording_save));
        }
    }

    public void showSaveRecordingDialog() {
        //Usage of Layoutinfalter with Custom layout: https://goo.gl/FNdUjN
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = dialogView.findViewById(R.id.edit1);

        dialogBuilder.setTitle(R.string.file_select_name);
        dialogBuilder.setMessage(getString(R.string.external_folder_saving));
        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                writeMidiJsonExternal(edt.getText().toString());
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void loadRecordings(View view) {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        properties.root = new File(preferences.getString(DATEIPFAD_KEY, Environment.getExternalStorageDirectory().getPath() + "/OrgelHelfer"));
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"json"};
        FilePickerDialog dialog = new FilePickerDialog(this, properties);
        dialog.setTitle(getString(R.string.file_select));
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
                        jsonObject = new JSONObject(jsonString.toString());
                    } catch (IOException e) {
                        Log.d(LOG_TAG, getString(R.string.error_recording_load) + e.toString());
                        e.printStackTrace();
                    } catch (JSONException e) {
                        Log.d(LOG_TAG, getString(R.string.error_recording_format_exception) + e.toString());
                        e.printStackTrace();
                    }
                    setRecording(jsonObject);
                }
            }
        });


    }

    private void setRecording(JSONObject recordingJson) {
        midiRecording = MidiRecording.createRecordingFromJson(recordingJson);
        //startPlayerService();
        final Button playButton = findViewById(R.id.playButton);
        playButton.setEnabled(true);
        Player.setIsRecordingPlaying(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playButton.setText(R.string.Play);
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Player.IsRecordingPlaying()) {
                    Player.setIsRecordingPlaying(false);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            playButton.setText(R.string.Play);
                        }
                    });
                } else {
                    Player.playRecording(midiRecording, null);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            playButton.setText(R.string.Stop);
                        }
                    });
                }
            }
        });
    }


}
