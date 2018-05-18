package de.ostfalia.mobile.orgelhelfer.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.ostfalia.mobile.orgelhelfer.midi.MidiConstants;

public class MidiRecording {
    private static final String LOG_TAG = MidiRecording.class.getSimpleName();
    long startingTimestamp;
    private List<MidiEvent> recordingList = new ArrayList<>();

    public MidiRecording(List<MidiEvent> events, long _startingTimestamp) {
        recordingList = events;
        startingTimestamp = _startingTimestamp;
    }

    public MidiRecording() {

    }

    public static MidiRecording createRecordingFromJson(JSONObject jsonObject) {
        final String BASE_ID = "Note";
        ArrayList<MidiEvent> notes = new ArrayList<>();
        long time = 0;
        try {
            for (int i = 0; i < jsonObject.length(); i++) {
                if (jsonObject.has(BASE_ID + i)) {
                    JSONObject obj = null;

                    obj = (JSONObject) jsonObject.get(BASE_ID + i);

                    byte type = MidiConstants.MessageTypes.valueOf(obj.getString("Type")).getType();
                    byte channel = (byte) obj.getInt("Channel");
                    byte pitch = (byte) obj.getInt("Pitch");
                    byte velocity = (byte) obj.getInt("Velocity");
                    long timestamp = obj.getLong("Timestamp");
                    MidiEvent note = new MidiEvent(type, channel, pitch, velocity, timestamp);
                    notes.add(note);

                }

            }
            if (jsonObject.has("recording")) {
                time = jsonObject.getLong("recording");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "Error While Parsing Json: " + e.toString());
        }
        MidiRecording recording = new MidiRecording(notes, time);
        return recording;
    }

    public List<MidiEvent> getRecordingList() {
        return recordingList;
    }

    public void setRecordingList(List<MidiEvent> events) {
        this.recordingList = events;
    }

    public long getDuration() {
        long start = recordingList.get(0).getTimestamp();
        long end = recordingList.get(recordingList.size() - 1).getTimestamp();
        return end - start;
    }

    public long getStartingTimestamp() {
        return startingTimestamp;
    }

    public void setStartingTimestamp(long _startingTimestamp) {
        startingTimestamp = _startingTimestamp;
    }
}
