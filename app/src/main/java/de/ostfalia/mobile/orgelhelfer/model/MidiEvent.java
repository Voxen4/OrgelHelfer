package de.ostfalia.mobile.orgelhelfer.model;


import android.support.annotation.NonNull;

import de.ostfalia.mobile.orgelhelfer.midi.MidiConstants;

/**
 * Created by kellerm on 23.04.2018.
 * Class representing a interpreted MidiKey Event, contains The Key Pressed and the "strength"?
 */

public class MidiEvent implements Comparable<MidiEvent>{
    public static MidiEvent MIDDLEC = new MidiEvent(MidiConstants.MessageTypes.STATUS_NOTE_ON.getType(), (byte) (0x90 + 0), (byte) 60, (byte) 127);

    private final byte mType;
    private byte channel; //MIDI channels 1-16 are encoded as 0-15.
    private byte pitch;
    private byte velocity; // between 0 and 127
    private long timestamp;
    //90 2D 1D: NoteOn(1, 45, 29)


    public MidiEvent(byte _mType, byte _channel, byte _pitch, byte _velocity) {
        mType = _mType;
        channel = _channel;
        pitch = _pitch;
        velocity = _velocity;
        timestamp = System.currentTimeMillis();
    }

    public MidiEvent(byte _mType, byte _channel, byte _pitch, byte _velocity, long _timestamp) {
        mType = _mType;
        channel = _channel;
        pitch = _pitch;
        velocity = _velocity;
        timestamp = _timestamp;
    }

    private byte[] getRaw() {
        byte[] raw = raw = new byte[3];
        raw[0] = (byte) ((mType << 4) + channel);
        raw[1] = pitch;
        raw[2] = velocity;
        return raw;
    }

    /**
     * TODO. Implementation. Hier muss entschieden werden ob das Event zurückgespielt werden soll oder nicht.
     * Diese Methode gibt an, ob das Element zurückgespielt werden sollte. Ist nur wichtig für Elemente aus einer Aufnahme.
     * @return : Wahheitswert ob das Element zurückgespielt werden soll
     */
    public boolean getSollZurueckspielenWerden() {
        return false;
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
        return MidiConstants.MessageTypes.getTypeByByte(mType) + "(" + channel + "," + pitch + "," + velocity + "," + timestamp + ")";
    }

    @Override
    public int compareTo(@NonNull MidiEvent o) {
        byte[] oRaw = o.getRaw();
        for(int i = 0; i < getRaw().length; i++) {
            if(oRaw[i] < this.getRaw()[i]) {
                return 1;
            } else if(oRaw[i] > this.getRaw()[i]) {
                return -1;
            }
        }
        return 0;
    }
}

