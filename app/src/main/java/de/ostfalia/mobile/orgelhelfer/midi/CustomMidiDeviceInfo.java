package de.ostfalia.mobile.orgelhelfer.midi;

import android.media.midi.MidiDeviceInfo;

/**
 * Created by kellerm on 28.04.2018.
 */

public class CustomMidiDeviceInfo {
    private MidiDeviceInfo midiDeviceInfo;

    public CustomMidiDeviceInfo(MidiDeviceInfo _midiDeficeInfo) {
        midiDeviceInfo = _midiDeficeInfo;
    }

    public String toString(){
        return midiDeviceInfo.getProperties().get("product").toString();
    }

    public MidiDeviceInfo getDevice(){
        return midiDeviceInfo;
    }
}
