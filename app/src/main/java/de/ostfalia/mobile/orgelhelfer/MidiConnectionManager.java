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

import java.util.ArrayList;


/**
 * Created by kellerm on 23.04.2018.
 */

class MidiConnectionManager extends MidiManager.DeviceCallback implements MidiManager.OnDeviceOpenedListener {
    private static final MidiConnectionManager ourInstance = new MidiConnectionManager();
    private MidiOutputPort outputPort;
    private MidiInputPort inputPort;
    private MidiManager midiManager;
    private final MidiSender midiSender;
    public ArrayList<MidiDeviceInfo> devices;

    static MidiConnectionManager getInstance() {
        return ourInstance;
    }

    private MidiConnectionManager() {
        devices = new ArrayList<>();
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

    @Override
    public void onDeviceAdded(MidiDeviceInfo device) {
        super.onDeviceAdded(device);
        devices.add(device);
    }

    @Override
    public void onDeviceRemoved(MidiDeviceInfo device) {
        super.onDeviceRemoved(device);
        devices.remove(device);
    }

    @Override
    public void onDeviceStatusChanged(MidiDeviceStatus status) {
        super.onDeviceStatusChanged(status);
    }

    @Override
    public void onDeviceOpened(MidiDevice midiDevice) {
        inputPort = midiDevice.openInputPort(0);//TODO Port index ?
        outputPort = midiDevice.openOutputPort(0);
        if (outputPort != null && MidiDataManager.getInstance().receiver != null) {
            outputPort.connect(MidiDataManager.getInstance().receiver);
        }
        if (midiSender != null&& MidiDataManager.getInstance().sender != null) {
            midiSender.connect(MidiDataManager.getInstance().sender);
        }
    }

    public void setMidiManager(MidiManager midiManager) {
        this.midiManager = midiManager;
    }


    public void setupDevices() {
        for (MidiDeviceInfo info : midiManager.getDevices()) {
            if (info != null) {
                devices.add(info);
            }
        }

    }


    public MidiInputPort getInputPort() {
        return inputPort;
    }

    public void connectToDevice(MidiDeviceInfo deviceInfo) {
        midiManager.openDevice(deviceInfo, this, new Handler(Looper.getMainLooper()));
    }
}
