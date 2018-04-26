package de.ostfalia.mobile.orgelhelfer;

import android.content.Context;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.Button;
import android.widget.ListView;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;

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
    public void textListView() throws InterruptedException {
        BaseActivity activity = mActivityRule.getActivity();
        MidiNote[] texts = new MidiNote[100];
        for (int i = 0; i < 100; i++) {
            MidiNote note = MidiNote.MIDDLEC;
            note.setTimestamp(note.getTimestamp() + 1);
            texts[i] = note;
            activity.onMidiData(note);
        }
        Thread.sleep(100);
        Assert.assertArrayEquals(texts, activity.log.toArray());
        ListView listView = activity.findViewById(R.id.list);
        Assert.assertEquals(100, listView.getAdapter().getCount());
        //Yes i'm paranoid, that's why im Ui testing here.

    }

    @Test
    public void saveJsonTest() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        BaseActivity activity = mActivityRule.getActivity();
        Button recordButton = activity.findViewById(R.id.recordButton);
        long timestamp = System.currentTimeMillis();
        activity.startRecording(recordButton);
        for (int i = 0; i < 100; i++) {
            MidiNote note = MidiNote.MIDDLEC;
            note.setTimestamp(note.getTimestamp() + 1);
            activity.onMidiData(note);
        }
        activity.startRecording(recordButton);
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "OrgelHelfer" + File.separator + "track.json";
        Assert.assertEquals(true, new File(path).exists());

    }


}
