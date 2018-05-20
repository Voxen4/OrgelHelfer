package de.ostfalia.mobile.orgelhelfer;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ListView;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.ostfalia.mobile.orgelhelfer.model.MidiEvent;
import de.ostfalia.mobile.orgelhelfer.model.MidiNote;
import de.ostfalia.mobile.orgelhelfer.model.MidiProgram;

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
        MidiEvent[] texts = new MidiEvent[200];
        for (int i = 0; i < texts.length / 2; i++) {
            MidiEvent note = MidiNote.MIDDLEC;
            note.setTimestamp(note.getTimestamp() + 1000);
            texts[i] = note;
            activity.onMidiData(note);
        }
        for (int i = 100; i < texts.length; i++) {
            MidiEvent note = MidiProgram.ProgramTest;
            note.setTimestamp(note.getTimestamp() + 1000);
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
    public void testListViewOnClick() throws InterruptedException {
        BaseActivity activity = mActivityRule.getActivity();
        MidiEvent[] texts = new MidiEvent[100];
        for (int i = 0; i < 100; i++) {
            MidiEvent note = MidiNote.MIDDLEC;
            note.setTimestamp(note.getTimestamp() + 1);
            texts[i] = note;
            activity.onMidiData(note);
        }
        Thread.sleep(100);
        final ListView listView = activity.findViewById(R.id.list);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean itemCLick = itemCLick = listView.performItemClick(listView, 2, listView.getItemIdAtPosition(2));
                Assert.assertEquals(true, itemCLick);
            }
        });
        //Yes i'm paranoid, that's why im Ui testing here.

    }



}
