package de.ostfalia.mobile.orgelhelfer;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import de.ostfalia.mobile.orgelhelfer.midi.MidiConstants;

public class BaseActivity extends AppCompatActivity implements MidiDataManager.OnMidiDataListener {

    private static final String LOG_TAG = BaseActivity.class.getSimpleName();
    private static final String TRACK_NUMBER = "TRACK_NUMBER";
    ArrayList<MidiNote> log = new ArrayList<>();
    SharedPreferences preference;
    private ListView listView;
    private boolean recording;
    private JSONObject jsonData;
    private int track;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MidiManager midiManager = (MidiManager) getSystemService(MIDI_SERVICE);
        checkPermissions();
        setupUi();
        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            // do MIDI stuff
            // Setup a menu to select an input source.
            MidiConnectionManager.getInstance().setMidiManager(midiManager);
            MidiConnectionManager.getInstance().setupDevices();
            MidiDataManager.getInstance().addOnMidiDataListener(this);
        } else {
            Log.d(LOG_TAG, "NO MIDI Support for this Device");
            noMidiSupportAlert(false).show();
        }
        preference = PreferenceManager.getDefaultSharedPreferences(this);
        track = preference.getInt(TRACK_NUMBER, -1);
    }

    private void setupUi() {

        listView = findViewById(R.id.list);
        ArrayAdapter adapter = new MidiEventArrayAdapter(this, R.layout.simple_list_item_slim, log);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View view,
                                            int position, long id) {
                        MidiNote event = (MidiNote) listView.getItemAtPosition(position);
                        MidiDataManager.getInstance().sendEvent(event);
                    }
                }
        );

        final Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter arrayAdapter =
                new ArrayAdapter<MidiDeviceInfo>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, MidiConnectionManager.getInstance().devices);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MidiConnectionManager.getInstance().connectToDevice((MidiDeviceInfo) spinner.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    public AlertDialog noMidiSupportAlert(boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(cancelable);
        builder.setTitle(R.string.no_midi_service_title);
        builder.setMessage(R.string.no_midi_service);
        return builder.create();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preference.edit();
        editor.putInt(TRACK_NUMBER, track);
        editor.apply();
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
            writeMidiJsonExternal();
        } else {
            jsonData = new JSONObject();
            try {
                jsonData.put("recording", ((Long) System.currentTimeMillis()).toString());
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

    public void debugCode(View view) {
        for (int i = 0; i < 100; i++) {
            MidiNote note = MidiNote.MIDDLEC;
            note.setTimestamp(note.getTimestamp() + 1);
            onMidiData(note);
        }
        Log.d(LOG_TAG, "Debug Code executed");
    }

    @Override
    public void onMidiData(final MidiNote event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, event.toString());
                log.add(event);
                ((MidiEventArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
                if (recording) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("Type", MidiConstants.MessageTypes.getTypeByByte(event.getmType()));
                        jsonObject.put("Channel", event.getChannel());
                        jsonObject.put("Pitch", event.getChannel());
                        jsonObject.put("Velocity", event.getChannel());
                        jsonObject.put("Timestamp", event.getTimestamp());
                        jsonData.put("Note" + jsonData.length(), jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d(LOG_TAG, "Error while adding Note to JSON");
                    }
                }

            }
        });
    }

    public void writeMidiJsonExternal() {
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
        File file = new File(folder + File.separator + "track" + ++track + ".json");
        try {
            output = new BufferedWriter(new FileWriter(file));
            output.write(jsonData.toString());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "Couldn't save Json Data");
        }
    }

    public JSONObject getJson() {
        return jsonData;
    }
}
