package de.ostfalia.mobile.orgelhelfer;

import android.content.Context;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import de.ostfalia.mobile.orgelhelfer.midi.MidiDataReceiver;
import de.ostfalia.mobile.orgelhelfer.midi.MidiScope;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Rule
    public ActivityTestRule<BaseActivity> mActivityRule =
            new ActivityTestRule(BaseActivity.class);

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("de.ostfalia.mobile.orgelhelfer", appContext.getPackageName());
    }

    @Test
    public void saveJsonTest() throws IOException, JSONException {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        MidiDataReceiver dataReceiver = (MidiDataReceiver) MidiScope.getmDeviceFramer().getmReceiver();
        long timestamp = System.currentTimeMillis();
        for (int i = 0;i < 100;i++){
            dataReceiver.addToJson(timestamp+i,"testraw"+i,"Interpreted");
        }
        BaseActivity activity = mActivityRule.getActivity();
        MidiScope.writeMidiJsonExternal();
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "OrgelHelfer"+File.separator+"track.json";
        Assert.assertEquals(true,new File(path).exists());
    }
}
