package de.ostfalia.mobile.orgelhelfer.model;

import de.ostfalia.mobile.orgelhelfer.midi.MidiConstants;

/**
 * Created by kellerm on 20.05.2018.
 */

public class MidiProgram extends MidiEvent {
    public static MidiProgram ProgramTest = new MidiProgram(MidiConstants.MessageTypes.STATUS_PROGRAM_CHANGE.getType(), new byte[]{(byte) 0, (byte) 0});

    public MidiProgram(byte _mType, byte[] _data) {
        super(_mType, _data);
    }

    public MidiProgram(byte _mType, byte[] _data, long _timestamp) {
        super(_mType, _data, _timestamp);
    }
}
