package de.ostfalia.mobile.orgelhelfer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.ostfalia.mobile.orgelhelfer.model.MidiNote;
import de.ostfalia.mobile.orgelhelfer.model.MidiRecording;

public class PlayerActivity extends AppCompatActivity {
    private static final String LOG_TAG = PlayerActivity.class.getSimpleName();
    public static MidiRecording recording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        TextView jsonText = findViewById(R.id.jsonText);
        Bundle extras = getIntent().getExtras();
        if (extras.containsKey("Song")) {
            try {
                recording = MidiRecording.createRecordingFromJson(new JSONObject(extras.get("Song").toString()));
            } catch (JSONException e) {
                e.printStackTrace();
                finish();
            }
            long duration = recording.getDuration();
            Button playButton = findViewById(R.id.playButton);
            jsonText.setText("Duration: " + duration);
            if (MidiConnectionManager.getInstance().getInputPort() != null) {
               /* Intent startIntent = new Intent(PlayerActivity.this, MidiPlayerService.class);
                startIntent.setAction(Constants.MAIN_ACTION);
                startService(startIntent);*/

            }
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<MidiNote> notes = recording.getRecordingList();
                            long lastTimestamp = 0;
                            long nextTimestamp = 0;
                            for (int i = 0; i < notes.size(); i++) {
                                if (MidiConnectionManager.getInstance().getInputPort() == null) {
                                    break;
                                }
                                if (!(i + 1 < notes.size())) {
                                    MidiDataManager.getInstance().sendEvent(notes.get(i));
                                    return;
                                }

                                MidiDataManager.getInstance().sendEvent(notes.get(i));
                                nextTimestamp = notes.get(i + 1).getTimestamp();
                                lastTimestamp = notes.get(i).getTimestamp();
                                long dif = nextTimestamp - lastTimestamp;
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
                    }).start();
                }
            });
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

}
