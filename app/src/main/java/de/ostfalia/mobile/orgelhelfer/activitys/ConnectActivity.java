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
import java.util.List;

import de.ostfalia.mobile.orgelhelfer.MidiConnectionManager;
import de.ostfalia.mobile.orgelhelfer.MidiDataManager;
import de.ostfalia.mobile.orgelhelfer.MidiEventArrayAdapter;
import de.ostfalia.mobile.orgelhelfer.R;
import de.ostfalia.mobile.orgelhelfer.db.App;
import de.ostfalia.mobile.orgelhelfer.db.Kategorie;
import de.ostfalia.mobile.orgelhelfer.db.Track;
import de.ostfalia.mobile.orgelhelfer.midi.CustomMidiDeviceInfo;
import de.ostfalia.mobile.orgelhelfer.model.MidiEvent;
import de.ostfalia.mobile.orgelhelfer.model.MidiRecording;
import de.ostfalia.mobile.orgelhelfer.util.Player;

import static android.widget.AdapterView.OnItemClickListener;
import static android.widget.AdapterView.OnItemSelectedListener;
import static de.ostfalia.mobile.orgelhelfer.activitys.SetupActivity.DATEIPFAD_KEY;

/**
 * Activtiy to handle the connecting to a MidiDevice if the Auto Connect connect's to a undesired MidiDevice.
 * Furthermore it Handles the Recording of {@link MidiRecording} it transfer's it into a JsonObject then stores
 * it in the DB{@link de.ostfalia.mobile.orgelhelfer.db.MyDatabase} and SD Card.
 * The Activity also handles showing incoming {@link MidiEvent} in a ListView
 */
public class ConnectActivity extends BaseActivity implements MidiDataManager.OnMidiDataListener, Player.SongStateCallback {

    private static final String LOG_TAG = ConnectActivity.class.getSimpleName();
    public static MidiRecording midiRecording;
    public ArrayList<MidiEvent> log = new ArrayList<>();
    private ListView listView;
    private Spinner spinner;
    private Button playButton;
    private boolean recording;
    private JSONObject jsonData;
    private List<Kategorie> genres;
    private ConnectActivity thizClazz;
    private String trackname = "";


    /**
     * OnCreate of the ConnectActivity, handles setting up Listeners, ArraysAdapters and ClickListeners
     *
     * @param savedInstanceState
     */
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

        //Making a room query and setting the result for the ui, really dirty(LiveData or Async better)
        new Thread(new Runnable() {
            @Override
            public void run() {
                genres = App.get().getDB().kategorieDao().getAll();
            }
        }).start();
        thizClazz = this;
    }

    /**
     * onPause removes the Activity from the {@link de.ostfalia.mobile.orgelhelfer.MidiDataManager.OnMidiDataListener}
     */
    @Override
    public void onPause() {
        super.onPause();
        MidiDataManager.getInstance().removeOnMidiDataListener(this);
    }


    /**
     * onPause adds the Activity back to the {@link de.ostfalia.mobile.orgelhelfer.MidiDataManager.OnMidiDataListener}
     */
    @Override
    public void onResume() {
        super.onResume();
        MidiDataManager.getInstance().addOnMidiDataListener(this);
        //stopService();
    }

    /**
     * Method used by the Start/Stop Recording, get's called on a Click.
     * It starts the Recording of the {@link MidiRecording} as a JsonObject and Calls the Dialog to Save the JsonObject{@see ConnectActivity#showSaveRecordingDialog}.
     * @param view
     */
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

    /**
     * Method from the {@link de.ostfalia.mobile.orgelhelfer.MidiDataManager.OnMidiDataListener},
     * MidiEvent's get Added to the current {@link MidiRecording} if it's being recorded currently
     * and added to the ListView of this Activity.
     * @param event
     */
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

    /**
     * Method getting called by the SaveDialog, saves the Current MidiRecording JsonObject on the SDCard with the given Name.
     * @param name
     */
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

    /**
     * Save Recording Dialog, displays a TextField where the User can enter a name for the Recording.
     * It also shows a Spinner with Genre's{@link Kategorie} for the Song.
     * If the User chooses to save the Recording the file gets written to the SdCard and the Combination of Genre + MidiRecording get's saved in the DB aswell.
     */
    public void showSaveRecordingDialog() {
        //Usage of Layoutinfalter with Custom layout: https://goo.gl/FNdUjN
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = dialogView.findViewById(R.id.edit1);
        final Spinner spinnerGenre = dialogView.findViewById(R.id.spinner1);
        if (genres != null) {
            final ArrayAdapter<Kategorie> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, genres);
            arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            spinnerGenre.setAdapter(arrayAdapter);
        }

        dialogBuilder.setTitle(R.string.file_select_name);
        dialogBuilder.setMessage(getString(R.string.external_folder_saving));
        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                writeMidiJsonExternal(edt.getText().toString());
                trackname = edt.getText().toString();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Object selectedKat = spinnerGenre.getSelectedItem();
                        if (selectedKat != null) {
                            Track track = new Track(trackname, jsonData.toString());
                            track.setKategorieFremdschluessel(((Kategorie) selectedKat).getKategorieID());
                            System.out.println("ID auswahl " + ((Kategorie) selectedKat).getKategorieID());
                            App.get().getDB().trackDao().insertOne(track);
                        } else {
                            App.get().getDB().trackDao().insertOne(new Track(trackname, jsonData.toString()));
                        }

                    }
                }).start();

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

    /**
     * Method getting called by the ClickListener of the LoadRecording Button, shows a FilePicker dialog where the User can pick a MidiRecording's Json Object
     * and calls {@link ConnectActivity#setRecording(JSONObject)} with it.
     * @param view
     */
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

    /**
     * Parses a Json Object to a MidiRecording and saves it in a variable, also set's the ClickListener for the Play Button and reset's it stae.
     * @param recordingJson
     */
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
                } else {
                    Player.playRecording(midiRecording, thizClazz);
                }
            }
        });
    }


    @Override
    public void songStateChanged(final Player.SongState state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state == Player.SongState.PLAYING) {
                    playButton.setText(R.string.Stop);
                } else {
                    playButton.setText(R.string.Play);
                }
            }
        });
    }
}
