package de.ostfalia.mobile.orgelhelfer;

import android.media.midi.MidiInputPort;
import android.media.midi.MidiReceiver;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.ostfalia.mobile.orgelhelfer.midi.MidiConstants;
import de.ostfalia.mobile.orgelhelfer.midi.MidiPrinter;

/**
 * Created by kellerm on 23.04.2018.
 */

class MidiDataManager {
    private static final MidiDataManager ourInstance = new MidiDataManager();
    private static final String LOG_TAG = MidiDataManager.class.getSimpleName();
    public MidiReceiver receiver;
    public MidiReceiver sender;
    private ArrayList<OnMidiDataListener> listeners = new ArrayList<>();

    private byte[] mBuffer = new byte[3];
    private int mCount;
    private byte mRunningStatus;
    private int mNeeded;
    private boolean mInSysEx;

    private static final long NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1);
    private long mStartTime;

    static MidiDataManager getInstance() {
        return ourInstance;
    }

    private MidiDataManager() {
        receiver = new Receiver();
        mStartTime = System.nanoTime();
    }

    /**
     * Notifying Listeners for MidiData, the send Data is already formatted
     *
     * @param midiNote
     */
    private void notifyDataListeners(MidiNote midiNote) {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onMidiData(midiNote, System.currentTimeMillis());
        }
    }

    public void addOnMidiDataListener(OnMidiDataListener listener) {
        listeners.add(listener);
    }

    //Input Port has to be opened first.
    public void sendEvent(MidiNote event) {
        //TODO Create parseable Event
        MidiInputPort inputPort = MidiConnectionManager.getInstance().getInputPort();
        if (sender == null) return;
        byte[] buffer = new byte[32];
        int numBytes = 0;
        buffer[numBytes++] = event.getChannel();//Not sure status byte ?
        buffer[numBytes++] = event.getPitch();
        buffer[numBytes++] = event.getVelocity();
        int offset = 0;
        /*int channel = 3; // MIDI channels 1-16 are encoded as 0-15.
        buffer[numBytes++] = (byte) (0x90 + (channel - 1)); // note on
        buffer[numBytes++] = (byte) 60; // pitch is middle C
        buffer[numBytes++] = (byte) 127; // max velocity
      */
// post is non-blocking
        try {
            inputPort.send(buffer, offset, numBytes);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "Couldn't send Event: " + event.toString() + ", Error: " + e.toString());
        }
    }

    public interface OnMidiDataListener {
        void onMidiData(MidiNote event, long timestamp);
    }

    private final class Receiver extends MidiReceiver {


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
         * @param data      a byte array containing the MIDI data
         * @param offset    the offset of the first byte of the data in the array to be processed
         * @param count     the number of bytes of MIDI data in the array to be processed
         * @param timestamp the timestamp of the message (based on {@link System#nanoTime}
         * @throws IOException
         */
        @Override
        public void onSend(byte[] data, int offset, int count, long timestamp) {
            int sysExStartOffset = (mInSysEx ? offset : -1);

            Log.d("MIDIFRAMER_DATA", Arrays.toString(data) + "," + offset + "," + count + "," + timestamp);
            for (int i = 0; i < count; i++) {
                final byte currentByte = data[offset];
                final int currentInt = currentByte & 0xFF;
                if (currentInt >= 0x80) { // status byte?
                    if (currentInt < 0xF0) { // channel message?
                        mRunningStatus = currentByte;
                        mCount = 1;
                        mNeeded = MidiConstants.getBytesPerMessage(currentByte) - 1;
                    } else if (currentInt < 0xF8) { // system common?
                        if (currentInt == 0xF0 /* SysEx Start */) {
                            // Log.i(TAG, "SysEx Start");
                            mInSysEx = true;
                            sysExStartOffset = offset;
                        } else if (currentInt == 0xF7 /* SysEx End */) {
                            // Log.i(TAG, "SysEx End");
                            if (mInSysEx) {
                                parseOnSend(data, sysExStartOffset,
                                        offset - sysExStartOffset + 1, timestamp);
                                mInSysEx = false;
                                sysExStartOffset = -1;
                            }
                        } else {
                            mBuffer[0] = currentByte;
                            mRunningStatus = 0;
                            mCount = 1;
                            mNeeded = MidiConstants.getBytesPerMessage(currentByte) - 1;
                        }
                    } else { // real-time?
                        // Single byte message interleaved with other data.
                        if (mInSysEx) {
                            parseOnSend(data, sysExStartOffset,
                                    offset - sysExStartOffset, timestamp);
                            sysExStartOffset = offset + 1;
                        }
                        parseOnSend(data, offset, 1, timestamp);
                    }
                } else { // data byte
                    if (!mInSysEx) {
                        mBuffer[mCount++] = currentByte;
                        if (--mNeeded == 0) {
                            if (mRunningStatus != 0) {
                                mBuffer[0] = mRunningStatus;
                            }
                            parseOnSend(mBuffer, 0, mCount, timestamp);
                            mNeeded = MidiConstants.getBytesPerMessage(mBuffer[0]) - 1;
                            mCount = 1;
                        }
                    }
                }
                ++offset;
            }


            // send any accumulatedSysEx data
            if (sysExStartOffset >= 0 && sysExStartOffset < offset) {
                parseOnSend(data, sysExStartOffset,
                        offset - sysExStartOffset, timestamp);
            }

        }

        private void parseOnSend(byte[] data, int sysExStartOffset, int i, long timestamp) {

            StringBuilder sb = new StringBuilder();
            if (timestamp == 0) {
                sb.append("-----0----: ");
            } else {
                long monoTime = timestamp - mStartTime;
                double seconds = (double) monoTime / NANOS_PER_SECOND;
                sb.append(String.format(Locale.US, "%10.3f: ", seconds));
            }
            String raw = MidiPrinter.formatBytes(data, sysExStartOffset, i);
            sb.append(raw);
            sb.append(": ");
            String interpreted = MidiPrinter.formatMessage(data, sysExStartOffset);
            sb.append(interpreted);
            String text = sb.toString();
            Log.i(LOG_TAG, text);
            /*try {
                addToJson(timestamp, raw, interpreted);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(LOG_TAG, "Error while Adding Json Object: " + e.toString());
            }*/
            //TODO PARSe to Event if possible
            if (MidiConstants.isAllActiveSensing(data, sysExStartOffset, i)) {
                return;
            }
            byte statusByte = data[sysExStartOffset];
            int status = statusByte & 0xFF;
            //TODO Hier gegebenenfalls die System Messages rausfiltern
            if (MidiPrinter.getType(status) == MidiConstants.MessageTypes.STATUS_NOTE_ON) {
                MidiNote temp = new MidiNote(MidiPrinter.getType(status).getType(), data[sysExStartOffset++], data[sysExStartOffset++], data[sysExStartOffset++]);
                MidiDataManager.getInstance().notifyDataListeners(temp);
            }
        }
    }


}
