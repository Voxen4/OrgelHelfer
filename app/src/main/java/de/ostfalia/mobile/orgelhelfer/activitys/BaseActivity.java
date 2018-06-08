package de.ostfalia.mobile.orgelhelfer.activitys;

import android.content.pm.PackageManager;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import de.ostfalia.mobile.orgelhelfer.MidiConnectionManager;
import de.ostfalia.mobile.orgelhelfer.midi.CustomMidiDeviceInfo;

public class BaseActivity extends AppCompatActivity implements MidiConnectionManager.OnDeviceChangedListener {
    protected List<CustomMidiDeviceInfo> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MidiManager midiManager = (MidiManager) getSystemService(MIDI_SERVICE);

        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            // do MIDI stuff
            // SetupActivity a menu to select an input source.
            MidiConnectionManager.getInstance().setMidiManager(midiManager);
            MidiConnectionManager.getInstance().addOnDevicesChangedListener(this);
            devices = getMidiDevices();
            if (!devices.isEmpty()) {
                CustomMidiDeviceInfo deviceInfo = devices.get(0);
                MidiConnectionManager.getInstance().connectToDevice(deviceInfo);
            }

        }
    }
    @Override
    public void onPause() {
        super.onPause();
        MidiConnectionManager.getInstance().removeOnDevicesChangedListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        MidiConnectionManager.getInstance().addOnDevicesChangedListener(this);
    }

    @Override
    public void onDevicesChanged() {
        devices = getMidiDevices();
        if (MidiConnectionManager.getInstance().getInputPort() != null && !devices.isEmpty()) {
            MidiConnectionManager.getInstance().connectToDevice(devices.get(0));
        }
    }

    public List<CustomMidiDeviceInfo> getMidiDevices() {
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
}
