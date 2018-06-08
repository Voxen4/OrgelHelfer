package de.ostfalia.mobile.orgelhelfer.model;

import org.json.JSONException;
import org.json.JSONObject;

import de.ostfalia.mobile.orgelhelfer.midi.MidiConstants;

/**
 * Created by kellerm on 20.05.2018.
 * Class representing a interpreted MidiNote Event, contains The channel,pitch and velocity of the Note
 */

public class MidiNote extends MidiEvent {
    public static MidiEvent MIDDLEC = new MidiNote(MidiConstants.MessageTypes.STATUS_NOTE_ON.getType(), (byte) (0x90 + 0), (byte) 60, (byte) 127);
    private byte channel; //MIDI channels 1-16 are encoded as 0-15.
    private byte pitch;
    private byte velocity; // between 0 and 127

    public MidiNote(byte _mType, byte[] data) {
        super(_mType, data);
        int sysExStartOffset = 0;
        channel = data[sysExStartOffset++];
        pitch = data[sysExStartOffset++];
        velocity = data[sysExStartOffset++];

    }

    public MidiNote(byte _mType, byte[] data, long timestamp) {
        super(_mType, data, timestamp);
        int sysExStartOffset = 0;
        channel = data[sysExStartOffset++];
        pitch = data[sysExStartOffset++];
        velocity = data[sysExStartOffset++];
    }

    public MidiNote(byte _mType, byte _channel, byte _pitch, byte _velocity) {
        super(_mType, new byte[]{_channel, _pitch, _velocity});
        channel = _channel;
        pitch = _pitch;
        velocity = _velocity;
    }

    public MidiNote(byte _mType, byte _channel, byte _pitch, byte _velocity, long _timestamp) {
        super(_mType, new byte[]{_channel, _pitch, _velocity}, _timestamp);
        channel = _channel;
        pitch = _pitch;
        velocity = _velocity;
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

    @Override
    public String toString() {
        return MidiConstants.MessageTypes.getTypeByByte(super.getmType()) + "(" + channel + "," + pitch + "," + velocity + "," + super.getTimestamp() + ")";
    }

    @Override
    public JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Type", MidiConstants.MessageTypes.getTypeByByte(super.getmType()));
        jsonObject.put("Channel", getChannel());
        jsonObject.put("Pitch", getPitch());
        jsonObject.put("Velocity", getVelocity());
        jsonObject.put("Timestamp", getTimestamp());
        return jsonObject;
    }
}
