package de.ostfalia.mobile.orgelhelfer;

import android.content.Context;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ListView;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

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

    @Test
    public void textListView() throws InterruptedException {
        BaseActivity activity = mActivityRule.getActivity();
        String[] texts = new String[100];
        for(int i = 0; i < 100;i++){
            String text = "Test: "+i;
            activity.log(text);
            texts[i] = text;
        }
        Thread.sleep(100);
        Assert.assertArrayEquals(texts,activity.log.toArray());
        ListView listView = (ListView)activity.findViewById(R.id.list);
        Assert.assertEquals(100,listView.getAdapter().getCount());
        //Yes i'm paranoid, that's why im Ui testing here.

    }

}
