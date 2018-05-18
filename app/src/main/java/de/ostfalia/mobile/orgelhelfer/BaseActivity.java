package de.ostfalia.mobile.orgelhelfer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import de.ostfalia.mobile.orgelhelfer.midi.CustomMidiDeviceInfo;
import de.ostfalia.mobile.orgelhelfer.midi.MidiConstants;
import de.ostfalia.mobile.orgelhelfer.model.Constants;
import de.ostfalia.mobile.orgelhelfer.model.MidiEvent;
import de.ostfalia.mobile.orgelhelfer.model.MidiRecording;
import de.ostfalia.mobile.orgelhelfer.services.MidiPlayerService;

import static android.widget.AdapterView.OnItemClickListener;
import static android.widget.AdapterView.OnItemSelectedListener;

public class BaseActivity extends AppCompatActivity implements MidiDataManager.OnMidiDataListener, MidiConnectionManager.OnDeviceChangedListener {

    private static final String LOG_TAG = BaseActivity.class.getSimpleName();
    public static MidiRecording midiRecording;
    ArrayList<MidiEvent> log = new ArrayList<>();
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
        checkPermissions();

        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            // do MIDI stuff
            // Setup a menu to select an input source.
            MidiConnectionManager.getInstance().setMidiManager(midiManager);
            MidiDataManager.getInstance().addOnMidiDataListener(this);
            MidiConnectionManager.getInstance().addOnDevicesChangedListener(this);
        } else {
            Log.d(LOG_TAG, "NO MIDI Support for this Device");
            noMidiSupportAlert(false).show();
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
        ArrayAdapter<CustomMidiDeviceInfo> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, getMidiDevices());
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
        MidiConnectionManager.getInstance().removeOnDevicesChangedListener(this);
        MidiDataManager.getInstance().removeOnMidiDataListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        //stopService();
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
            showSaveRecordingDialog();
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

    @Override
    public void onMidiData(final MidiEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, event.toString());
                log.add(0, event);
                ((MidiEventArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
                if (recording) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("Type", MidiConstants.MessageTypes.getTypeByByte(event.getmType()));
                        jsonObject.put("Channel", event.getChannel());
                        jsonObject.put("Pitch", event.getPitch());
                        jsonObject.put("Velocity", event.getVelocity());
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


    @Override
    public void onDevicesChanged() {
        Spinner spinner = findViewById(R.id.spinner);
        spinner.getAdapter();
        ArrayAdapter<CustomMidiDeviceInfo> arrayAdapter =
                new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, getMidiDevices());
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
            Log.d(LOG_TAG, "Couldn't save Json Data");
        }
    }

    public void showSaveRecordingDialog() {
        //Usage of Layoutinfalter with Custom layout: https://goo.gl/FNdUjN
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = dialogView.findViewById(R.id.edit1);

        dialogBuilder.setTitle("Choose a Filename");
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
        properties.root = new File(Environment.getExternalStorageDirectory().getPath() + "/OrgelHelfer");
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"json"};
        FilePickerDialog dialog = new FilePickerDialog(this, properties);
        dialog.setTitle("Select a File");
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
                        Log.d(LOG_TAG, "Error Loading Recording: " + e.toString());
                        e.printStackTrace();
                    } catch (JSONException e) {
                        Log.d(LOG_TAG, "Error Loading Recording Wrong Json Format: " + e.toString());
                        e.printStackTrace();
                    }
                    setRecording(jsonObject);
                }
            }
        });


    }

    private void setRecording(JSONObject recordingJson) {
        midiRecording = MidiRecording.createRecordingFromJson(recordingJson);
        startPlayerService();
        final Button playButton = findViewById(R.id.playButton);
        playButton.setEnabled(true);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MidiPlayerService.IS_SERVICE_RUNNING) {
                    startPlayerService();
                }
                if (MidiPlayerService.IS_RECORDING_PLAYING) {
                    Intent stopIntent = new Intent(getApplicationContext(), MidiPlayerService.class);
                    stopIntent.setAction(Constants.STOP_PLAYING_RECORDING);
                    startService(stopIntent);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            playButton.setText("Play");
                        }
                    });
                } else {
                    Intent playIntent = new Intent(getApplicationContext(), MidiPlayerService.class);
                    playIntent.setAction(Constants.START_PLAYING_RECORDING);
                    startService(playIntent);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            playButton.setText("Stop");
                        }
                    });
                }
            }
        });
    }

    public void startPlayerService() {
        Intent startIntent = new Intent(BaseActivity.this, MidiPlayerService.class);
        startIntent.setAction(Constants.MAIN_ACTION);
        startService(startIntent);

    }
    public ArrayList<CustomMidiDeviceInfo> getMidiDevices() {
        MidiManager midiManager = (MidiManager) getSystemService(MIDI_SERVICE);
        ArrayList<CustomMidiDeviceInfo> list = new ArrayList<>();
        if (midiManager == null) {
            return list;
        }
        MidiDeviceInfo[] infos = midiManager.getDevices();
        for (int i = 0; i < infos.length; i++) {
            list.add(new CustomMidiDeviceInfo(infos[i]));
        }
        return list;
    }

    public void stopService() {
        Intent stopServiceIntent = new Intent(getApplicationContext(), MidiPlayerService.class);
        stopServiceIntent.setAction(Constants.STOP_MAIN_ACTION);
        startService(stopServiceIntent);
    }

    public JSONObject getJson() {
        return jsonData;
    }
}
