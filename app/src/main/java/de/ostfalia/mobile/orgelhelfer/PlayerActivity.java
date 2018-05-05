package de.ostfalia.mobile.orgelhelfer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import de.ostfalia.mobile.orgelhelfer.model.Constants;
import de.ostfalia.mobile.orgelhelfer.model.MidiRecording;
import de.ostfalia.mobile.orgelhelfer.services.MidiPlayerService;

public class PlayerActivity extends AppCompatActivity {
    private static final String LOG_TAG = PlayerActivity.class.getSimpleName();
    public static MidiRecording recording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
            //Dirty
        }
        if (extras.containsKey("Song")) {
            try {
                recording = MidiRecording.createRecordingFromJson(new JSONObject(extras.get("Song").toString()));
            } catch (JSONException e) {
                e.printStackTrace();
                finish();
            }
            startPlayerService();
            final Button playButton = findViewById(R.id.playButton);

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!MidiPlayerService.IS_SERVICE_RUNNING) {
                        startPlayerService();
                    }
                    if (MidiPlayerService.IS_RECORDING_PLAYING) {
                        Intent stopIntent = new Intent(getApplicationContext(), MidiPlayerService.class);
                        stopIntent.setAction(Constants.STOP_PLAYING_RECORDING);
                        startService(stopIntent);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playButton.setText("Play");
                            }
                        });
                    } else {
                        Intent playIntent = new Intent(getApplicationContext(), MidiPlayerService.class);
                        MidiPlayerService.IS_RECORDING_PLAYING = true;
                        playIntent.setAction(Constants.START_PLAYING_RECORDING);
                        startService(playIntent);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playButton.setText("Stop");
                            }
                        });
                    }
                }
            });
        }
    }

    public void startPlayerService() {
        Intent startIntent = new Intent(PlayerActivity.this, MidiPlayerService.class);
        startIntent.setAction(Constants.MAIN_ACTION);
        startService(startIntent);

    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
