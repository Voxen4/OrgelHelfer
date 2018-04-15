package de.ostfalia.mobile.orgelhelfer.midi;

import android.media.midi.MidiReceiver;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/*
Class to implement the Abstract Class MidiReceiver and manage data coming from the midi Interface.
 */
public class MidiDataReceiver extends MidiReceiver {

    private static final String LOG_TAG = MidiReceiver.class.getSimpleName();
    private static final long NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1);
    private long mStartTime;
    private ScopeLogger mLogger;
    private JSONObject jsonData;

    public MidiDataReceiver() {
        jsonData = new JSONObject();
        try {
            jsonData.put("recording", ((Long) System.currentTimeMillis()).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public MidiDataReceiver(ScopeLogger logger) {
        jsonData = new JSONObject();
        try {
            jsonData.put("recording", ((Long) System.currentTimeMillis()).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mStartTime = System.nanoTime();
        mLogger = logger;
    }

    /**
     * Called whenever the receiver is passed new MIDI data.
     * Subclasses override this method to receive MIDI data.
     * May fail if count exceeds {@link #getMaxMessageSize}.
     * <p>
     * NOTE: the msg array parameter is only valid within the context of this call.
     * The msg bytes should be copied by the receiver rather than retaining a reference
     * to this parameter.
     * Also, modifying the contents of the msg array parameter may result in other receivers
     * in the same application receiving incorrect values in their {link #onSend} method.
     *
     * @param data       a byte array containing the MIDI data
     * @param offset    the offset of the first byte of the data in the array to be processed
     * @param count     the number of bytes of MIDI data in the array to be processed
     * @param timestamp the timestamp of the message (based on {@link java.lang.System#nanoTime}
     * @throws IOException
     */
    @Override
    public void onSend(byte[] data, int offset, int count, long timestamp) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (timestamp == 0) {
            sb.append("-----0----: ");
        } else {
            long monoTime = timestamp - mStartTime;
            double seconds = (double) monoTime / NANOS_PER_SECOND;
            sb.append(String.format(Locale.US, "%10.3f: ", seconds));
        }
        String raw = MidiPrinter.formatBytes(data, offset, count);
        sb.append(raw);
        sb.append(": ");
        String interpreted = MidiPrinter.formatMessage(data, offset);
        sb.append(interpreted);
        String text = sb.toString();
        mLogger.log(text);
        Log.i(LOG_TAG, text);
        try {
            addToJson(timestamp, raw, interpreted);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "Error while Adding Json Object: " + e.toString());
        }
    }


    public void addToJson(long timestamp, String raw, String interpreted) throws JSONException {
        JSONObject currentDataJson = new JSONObject();
        Log.d(LOG_TAG, "New MIDI Key Added");
        currentDataJson.put("timestamp", timestamp);
        currentDataJson.put("raw", raw);
        currentDataJson.put("interpreted", interpreted);
        jsonData.put("Note"+raw, currentDataJson);
    }

    public JSONObject getJsonData(){
        return jsonData;
    }
}
