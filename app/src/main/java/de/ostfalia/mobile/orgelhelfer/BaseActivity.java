package de.ostfalia.mobile.orgelhelfer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.os.Bundle;
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

import java.util.ArrayList;

public class BaseActivity extends AppCompatActivity implements MidiDataManager.OnMidiDataListener {

    private static final String LOG_TAG = BaseActivity.class.getSimpleName();
    private ListView listView;
    ArrayList<MidiEvent> log = new ArrayList<>();

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
                        MidiEvent event = (MidiEvent) listView.getItemAtPosition(position);
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
        Log.d(LOG_TAG,"Switched recording state");
        //TODO Add recording
    }

    @Override
    public void onMidiData(final MidiEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, event.getUrl());
                log.add(event);
            }
        });
    }

    public static void writeMidiJsonExternal() {
       /* Writer output = null;
        JSONObject jsonData = ((MidiDataReceiver) mDeviceFramer.getmReceiver()).getJsonData();
        if (jsonData == null) {
            Log.d(LOG_TAG,"Json data is null");
            return;
        }
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "OrgelHelfer";
        File folder = new File(path + File.separator);
        if (!folder.exists()) {
            folder.mkdir();
        }
        File file = new File(folder + File.separator + "track.json");
        file.delete();
        try {
            output = new BufferedWriter(new FileWriter(file));
            output.write(jsonData.toString());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "Couldn't save Json Data");
        }*/
    }
}
