package de.ostfalia.mobile.orgelhelfer;


import de.ostfalia.mobile.orgelhelfer.midi.MidiConstants;

/**
 * Created by kellerm on 23.04.2018.
 * Class representing a interpreted MidiKey Event, contains The Key Pressed and the "strength"?
 */

public class MidiNote {
    public static MidiNote MIDDLEC = new MidiNote(MidiConstants.MessageTypes.STATUS_NOTE_ON.getType(), (byte) (0x90 + 2), (byte) 60, (byte) 127);

    private final byte mType;
    private byte channel; //MIDI channels 1-16 are encoded as 0-15.
    private byte pitch;
    private byte velocity; // between 0 and 127
    private long timestamp;
    //90 2D 1D: NoteOn(1, 45, 29)


    public MidiNote(byte _mType, byte _channel, byte _pitch, byte _velocity) {
        mType = _mType;
        channel = _channel;
        pitch = _pitch;
        velocity = _velocity;
        timestamp = System.currentTimeMillis();
    }

    public byte getmType() {
        return mType;
    }

    public byte getChannel() {
        return channel;
    }

    public byte getPitch() {
        return pitch;
    }

    public byte getVelocity() {
        return velocity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long _timestamp) {
        timestamp = _timestamp;
    }

    public String toString() {
        return "Type: " + MidiConstants.MessageTypes.getTypeByByte(mType) + "(" + channel + "," + pitch + "," + velocity + "," + timestamp + ")";
    }

}

