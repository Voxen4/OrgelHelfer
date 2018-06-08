package de.ostfalia.mobile.orgelhelfer.util;

import android.util.Log;

import java.util.List;

import de.ostfalia.mobile.orgelhelfer.MidiConnectionManager;
import de.ostfalia.mobile.orgelhelfer.MidiDataManager;
import de.ostfalia.mobile.orgelhelfer.model.MidiEvent;
import de.ostfalia.mobile.orgelhelfer.model.MidiRecording;


/**
 * Created by mkeller on 01.06.2018.
 * Helper Class to Play a MidiRecording, creates a Thread and loops trough the MidiEvents in the MidiRecording.
 * Callbacks on Start of a MidiRecording and after it Stopped. {@see SongStateCallback}
 */

public class Player {

    private static final String LOG_TAG = Player.class.getSimpleName();
    private static boolean IS_RECORDING_PLAYING = false;

    /**
     * Method to start playing a MidiRecording
     *
     * @param recording  - The MidiRecording
     * @param callback   - Callback Listener
     * @param startDelay - Delay before Recording starts Playing.
     */
    public static synchronized void playRecording(final MidiRecording recording, final SongStateCallback callback, final int startDelay) {
        Log.d(LOG_TAG, "Start Playing the Recording");

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (startDelay != 0) {
                    try {
                        Thread.sleep(startDelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.d(LOG_TAG, "Exception while delaying Playing: " + e.toString());
                    }
                }
                IS_RECORDING_PLAYING = true;
                notifyCallback(callback, SongState.PLAYING);
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
                IS_RECORDING_PLAYING = false;
                notifyCallback(callback, SongState.STOPPED);

            }
        }).

                start();
    }

    /**
     * Method to start playing a MidiRecording
     * @param recording - The MidiRecording
     * @param callback - Callback Listener
     */
    public static void playRecording(final MidiRecording recording, final SongStateCallback callback) {
        playRecording(recording, callback, 0);
    }

    /**
     * Notifys the Callback if it's not null of the given state
     * @param callback - Callback Listener
     * @param state - {@link SongState}
     */
    private static void notifyCallback(SongStateCallback callback, SongState state) {
        if (callback != null) {
            Log.d(LOG_TAG, "New Player State " + state.toString());
            callback.songStateChanged(state);
        }
    }

    public static boolean IsRecordingPlaying() {
        return IS_RECORDING_PLAYING;
    }

    public static void setIsRecordingPlaying(boolean _is) {
        IS_RECORDING_PLAYING = _is;
    }

    public enum SongState {
        PLAYING, STOPPED
    }

    /**
     * Interface to provide a Callback to {@link SongState} changes
     */
    public interface SongStateCallback {
        void songStateChanged(SongState state);
    }
}
