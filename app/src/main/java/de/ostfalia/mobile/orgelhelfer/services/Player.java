package de.ostfalia.mobile.orgelhelfer.services;

import android.util.Log;

import java.util.List;

import de.ostfalia.mobile.orgelhelfer.MidiConnectionManager;
import de.ostfalia.mobile.orgelhelfer.MidiDataManager;
import de.ostfalia.mobile.orgelhelfer.model.MidiEvent;
import de.ostfalia.mobile.orgelhelfer.model.MidiRecording;


/**
 * Created by Aaron on 01.06.2018.
 */

public class Player {

    private static final String LOG_TAG = Player.class.getSimpleName();
    private static boolean IS_RECORDING_PLAYING = false;

    public static void playRecording(final MidiRecording recording) {


        Log.d(LOG_TAG, "Start Playing the Recording");
        IS_RECORDING_PLAYING = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<MidiEvent> notes = recording.getRecordingList();
                long lastTimestamp = 0;
                long nextTimestamp = 0;
                for (int i = 0; i < notes.size(); i++) {
                    if (MidiConnectionManager.getInstance().getInputPort() == null || !IS_RECORDING_PLAYING) {
                        break;
                    }
                    if (i + 1 >= notes.size()) {
                        MidiDataManager.getInstance().sendEvent(notes.get(i));
                        break;
                    }
                    MidiDataManager.getInstance().sendEvent(notes.get(i));
                    //if (!(notes.get(i).getmType() == MidiConstants.MessageTypes.STATUS_PROGRAM_CHANGE.getType())) {
                    //  MidiDataManager.getInstance().sendEvent(notes.get(i));
                    //}
                    nextTimestamp = notes.get(i + 1).getTimestamp();
                    lastTimestamp = notes.get(i).getTimestamp();
                    long dif = nextTimestamp - lastTimestamp;
                    Log.d(LOG_TAG, "Time until next Note: " + dif);
                    if (dif > 0) {
                        try {
                            Thread.sleep(dif);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Log.d(LOG_TAG, "rip");
                        }
                    }

                }
            }
        }).

                start();
    }

    public static boolean IsRecordingPlaying() {
        return IS_RECORDING_PLAYING;
    }

    public static void setIsRecordingPlaying(boolean _is) {
        IS_RECORDING_PLAYING = _is;
    }
}
