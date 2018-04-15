/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ostfalia.mobile.orgelhelfer.midi;

import android.media.midi.MidiDeviceService;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiReceiver;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Virtual MIDI Device that logs messages to a ScopeLogger.
 */

public class MidiScope extends MidiDeviceService {

    private static final String LOG_TAG = MidiDeviceService.class.getSimpleName();
    private static ScopeLogger mScopeLogger;
    private MidiReceiver mInputReceiver = new MyReceiver();
    private static MidiFramer mDeviceFramer;

    @Override
    public MidiReceiver[] onGetInputPortReceivers() {
        return new MidiReceiver[] { mInputReceiver };
    }

    public static void setScopeLogger(ScopeLogger logger) {
        if (logger != null) {
            // Receiver that prints the messages.
            MidiDataReceiver loggingReceiver = new MidiDataReceiver(logger);
            mDeviceFramer = new MidiFramer(loggingReceiver);
        }
        mScopeLogger = logger;
    }

    private static class MyReceiver extends MidiReceiver {
        @Override
        public void onSend(byte[] data, int offset, int count,
                long timestamp) throws IOException {
            if (mScopeLogger != null) {
                // Send raw data to be parsed into discrete messages.
                mDeviceFramer.send(data, offset, count, timestamp);
            }
        }
    }

    /**
     * This will get called when clients connect or disconnect.
     * Log device information.
     */
    @Override
    public void onDeviceStatusChanged(MidiDeviceStatus status) {
        if (mScopeLogger != null) {
            if (status.isInputPortOpen(0)) {
                mScopeLogger.log("=== connected ===");
                String text = MidiPrinter.formatDeviceInfo(
                        status.getDeviceInfo());
                mScopeLogger.log(text);
            } else {
                mScopeLogger.log("--- disconnected ---");
            }
        }
    }

    public static void writeMidiJsonExternal() {
        Writer output = null;
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "OrgelHelfer";
        File folder = new File(path + File.separator);
        if (!folder.exists()) {
            folder.mkdir();
        }
        File file = new File(folder + File.separator + "track.json");
        try {
            output = new BufferedWriter(new FileWriter(file));
            output.write(((MidiDataReceiver) mDeviceFramer.getmReceiver()).getJsonData().toString());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "Couldn't save Json Data");
        }
    }

    public static ScopeLogger getmScopeLogger() {
        return mScopeLogger;
    }

    public MidiReceiver getmInputReceiver() {
        return mInputReceiver;
    }

    public static MidiFramer getmDeviceFramer() {
        return mDeviceFramer;
    }
}
