package de.ostfalia.mobile.orgelhelfer.model;

import java.util.ArrayList;
import java.util.List;

public class MidiRecording {
    int notepos;
    private List<MidiNote> recordingList = new ArrayList<>();

    public MidiRecording(List<MidiNote> events, long _startingTimestamp) {
        recordingList = events;
        long startingTimestamp = _startingTimestamp;
    }

    public MidiRecording() {

    }

    public MidiNote get() {
        return recordingList.get(notepos++);
    }

    public void setRecordingList(List<MidiNote> events) {
        this.recordingList = events;
    }

    public long getDuration() {
        long endingTimestamp = recordingList.get(recordingList.size() - 1).getTimestamp();
        long startingTimestamp = getStartingTimestamp();
        return endingTimestamp - startingTimestamp;
    }

    public long getStartingTimestamp() {
        return recordingList.get(0).getTimestamp();
    }

    public void setStartingTimestamp(long _startingTimestamp) {
        long startingTimestamp = _startingTimestamp;
    }

}
