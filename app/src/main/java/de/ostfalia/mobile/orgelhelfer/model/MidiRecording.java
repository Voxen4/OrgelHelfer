package de.ostfalia.mobile.orgelhelfer.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.ostfalia.mobile.orgelhelfer.midi.MidiConstants;
import de.ostfalia.mobile.orgelhelfer.midi.MidiPrinter;

public class MidiRecording {
    private static final String LOG_TAG = MidiRecording.class.getSimpleName();
    long startingTimestamp;
    private List<MidiEvent> recordingList = new ArrayList<>();

    public MidiRecording(List<MidiEvent> events, long _startingTimestamp) {
        recordingList = events;
        startingTimestamp = _startingTimestamp;
    }

    public static MidiRecording createRecordingFromJson(JSONObject jsonObject) {
        final String BASE_ID = "Event";
        ArrayList<MidiEvent> notes = new ArrayList<>();
        long time = 0;
        try {
            for (int i = 0; i < jsonObject.length(); i++) {
                if (jsonObject.has(BASE_ID + i)) {
                    JSONObject obj = null;

                    obj = (JSONObject) jsonObject.get(BASE_ID + i);
                    MidiEvent event = null;
                    byte type;
                    long timestamp;
                    switch (MidiPrinter.getType(obj.getInt("Type"))) {

                        case STATUS_NOTE_ON:
                            type = MidiConstants.MessageTypes.valueOf(obj.getString("Type")).getType();
                            byte channel = (byte) obj.getInt("Channel");
                            byte pitch = (byte) obj.getInt("Pitch");
                            byte velocity = (byte) obj.getInt("Velocity");
                            timestamp = obj.getLong("Timestamp");
                            event = new MidiNote(type, channel, pitch, velocity, timestamp);
                            break;
                        case STATUS_PROGRAM_CHANGE:
                            type = MidiConstants.MessageTypes.valueOf(obj.getString("Type")).getType();
                            String response = obj.getString("Data");
                            String[] byteValues = response.substring(1, response.length() - 1).split(",");
                            byte[] bytes = new byte[byteValues.length];

                            for (int j = 0, len = bytes.length; j < len; j++) {//ayy
                                bytes[j] = Byte.parseByte(byteValues[j].trim());
                            }
                            timestamp = obj.getLong("Timestamp");
                            event = new MidiProgram(type, bytes, timestamp);
                            break;
                    }
                    notes.add(event);

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
