package de.ostfalia.mobile.orgelhelfer;

import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.media.midi.MidiSender;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import de.ostfalia.mobile.orgelhelfer.midi.CustomMidiDeviceInfo;


/**
 * Created by kellerm on 23.04.2018.
 * Singelton handleing the Connection to a MidiDevice, opens it ports and provides
 * a Listener to notify Classes when there is a Change of available MidiDevices
 */

public class MidiConnectionManager extends MidiManager.DeviceCallback implements MidiManager.OnDeviceOpenedListener {
    private static final MidiConnectionManager ourInstance = new MidiConnectionManager();
    private static final String LOG_TAG = MidiConnectionManager.class.getSimpleName();
    private MidiSender midiSender;
    private MidiOutputPort outputPort;
    private MidiInputPort inputPort;
    private MidiManager midiManager;
    private ArrayList<OnDeviceChangedListener> listeners = new ArrayList<>();

    private MidiConnectionManager() {
        midiSender = new MidiSender() {
            @Override
            public void onConnect(MidiReceiver receiver) {
                MidiDataManager.getInstance().sender = receiver;
            }

            @Override
            public void onDisconnect(MidiReceiver receiver) {
                MidiDataManager.getInstance().sender = null;
            }
        };
    }

    public static MidiConnectionManager getInstance() {
        return ourInstance;
    }

    @Override
    public void onDeviceAdded(MidiDeviceInfo device) {
        super.onDeviceAdded(device);
        notifyDevicesChangedListeners();
        Log.d(LOG_TAG, "Device Added: " + device.toString());
    }

    @Override
    public void onDeviceRemoved(MidiDeviceInfo device) {
        super.onDeviceRemoved(device);
        notifyDevicesChangedListeners();
        Log.d(LOG_TAG, "Device Removed: " + device.toString());
    }

    @Override
    public void onDeviceStatusChanged(MidiDeviceStatus status) {
        super.onDeviceStatusChanged(status);
        Log.d(LOG_TAG, "Device Status Changed: " + status.toString());
    }

    private void notifyDevicesChangedListeners() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onDevicesChanged();
        }
    }

    @Override
    public void onDeviceOpened(MidiDevice midiDevice) {
        Log.d(LOG_TAG, "Device Opened trying to open Ports");
        try {
            if (outputPort != null) {
                outputPort.close();
            }
            if (inputPort != null) {
                inputPort.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "Error Closing port's before opening them: " + e.toString());
        }
        outputPort = midiDevice.openOutputPort(0);
        inputPort = midiDevice.openInputPort(0);
        if (outputPort != null && MidiDataManager.getInstance().receiver != null) {
            outputPort.connect(MidiDataManager.getInstance().receiver);
        }
        if (midiSender != null && MidiDataManager.getInstance().sender != null) {
            midiSender.connect(MidiDataManager.getInstance().sender);
        }
    }

    public void setMidiManager(MidiManager midiManager) {
        if (this.midiManager == null) {
            this.midiManager = midiManager;
            midiManager.registerDeviceCallback(this, new Handler(Looper.getMainLooper()));
        }
    }

    public void addOnDevicesChangedListener(OnDeviceChangedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeOnDevicesChangedListener(OnDeviceChangedListener listener) {
        listeners.remove(listener);
    }

    public MidiInputPort getInputPort() {
        return inputPort;
    }

    public void connectToDevice(CustomMidiDeviceInfo deviceInfo) {
        Log.d(LOG_TAG, "Connecting to device: " + deviceInfo);
        midiManager.openDevice(deviceInfo.getDevice(), this, new Handler(Looper.getMainLooper()));
    }


    public interface OnDeviceChangedListener {
        void onDevicesChanged();
    }

}
