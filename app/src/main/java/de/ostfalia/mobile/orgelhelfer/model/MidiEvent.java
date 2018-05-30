package de.ostfalia.mobile.orgelhelfer.model;


import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import de.ostfalia.mobile.orgelhelfer.dtw.DtwComparable;
import de.ostfalia.mobile.orgelhelfer.midi.MidiConstants;

/**
 * Created by kellerm on 23.04.2018.
 * Class representing a interpreted MidiKey Event, contains The Key Pressed and the "strength"?
 */

public class MidiEvent implements DtwComparable<MidiEvent>{

    private final byte mType;
    private final byte[] data;
    private long timestamp;
    //90 2D 1D: NoteOn(1, 45, 29)


    public MidiEvent(byte _mType, byte[] _data) {
        mType = _mType;
        data = _data;
        timestamp = System.currentTimeMillis();
    }

    public MidiEvent(byte _mType, byte[] _data, long _timestamp) {
        mType = _mType;
        data = _data;
        timestamp = _timestamp;
    }


    public byte[] getRaw() {
        return data;
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

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean listenTo() {
        if(this instanceof MidiNote) {
            return true;
        } else {
            return false;
        }
    }

    public void setTimestamp(long _timestamp) {
        timestamp = _timestamp;
    }

    public String toString() {
        return MidiConstants.MessageTypes.getTypeByByte(mType) + "(" + Arrays.toString(data) + timestamp + ")";
    }

    @Override
    public int dtwCompareTo(MidiEvent o) {
        if(mType == o.mType && data[1] == o.data[1]) {
            return 0;
        }
        return 1;
    }

    @Override
    public int compareTo(@NonNull MidiEvent o) {
        if(o.getTimestamp() < this.getTimestamp()) {
            return 1;
        } else if(o.getTimestamp() > this.getTimestamp()) {
            return -1;
        }
        return 0;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Type", MidiConstants.MessageTypes.getTypeByByte(getmType()));
        jsonObject.put("Data", Arrays.toString(getRaw()));
        jsonObject.put("Timestamp", getTimestamp());
        return jsonObject;
    }
}

