package de.ostfalia.mobile.orgelhelfer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.media.midi.MidiReceiver;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import de.ostfalia.mobile.orgelhelfer.adapter.MidiDeviceAdapter;
import de.ostfalia.mobile.orgelhelfer.midi.MidiDataReceiver;
import de.ostfalia.mobile.orgelhelfer.midi.MidiFramer;
import de.ostfalia.mobile.orgelhelfer.midi.MidiOutputPortSelector;
import de.ostfalia.mobile.orgelhelfer.midi.MidiPortWrapper;
import de.ostfalia.mobile.orgelhelfer.midi.MidiScope;
import de.ostfalia.mobile.orgelhelfer.midi.ScopeLogger;

public class BaseActivity extends AppCompatActivity implements ScopeLogger {

    private static final String LOG_TAG = BaseActivity.class.getSimpleName();
    private static MidiManager midiManager;
    private MidiOutputPortSelector mLogSenderSelector;
    private MidiReceiver midiReceiver;
    private MidiFramer midiFramer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        midiManager = (MidiManager) getSystemService(MIDI_SERVICE);
        if (!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            // do MIDI stuff
            Log.d(LOG_TAG, "NO MIDI Support for this Device");
            noMidiSupportAlert(false).show();
        }
        //Receiver of the converted MidiData
        midiReceiver = new MidiDataReceiver();
        //Converter for MidiData
        midiFramer = new MidiFramer(midiReceiver);
        // Setup a menu to select an input source.
        mLogSenderSelector = new MidiOutputPortSelector(midiManager, this, R.id.spinner) {
            @Override
            public void onPortSelected(final MidiPortWrapper wrapper) {
                super.onPortSelected(wrapper);
            }
        };
        mLogSenderSelector.getSender().connect(midiFramer);
        MidiScope.setScopeLogger(this);
        checkPermissions();
    }

    public MidiManager getMidiManager() {
        return midiManager;
    }

    public AlertDialog noMidiSupportAlert(boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(cancelable);
        builder.setTitle(R.string.no_midi_service_title);
        builder.setMessage(R.string.no_midi_service);
        return builder.create();
    }

    @Override
    public void log(String text) {
        Log.d(LOG_TAG, text);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MidiScope.writeMidiJsonExternal();
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

}
