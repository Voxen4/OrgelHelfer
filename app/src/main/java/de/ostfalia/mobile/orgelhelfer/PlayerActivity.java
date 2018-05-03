package de.ostfalia.mobile.orgelhelfer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.ostfalia.mobile.orgelhelfer.midi.MidiConstants;
import de.ostfalia.mobile.orgelhelfer.model.Constants;
import de.ostfalia.mobile.orgelhelfer.model.MidiNote;
import de.ostfalia.mobile.orgelhelfer.model.MidiRecording;
import de.ostfalia.mobile.orgelhelfer.services.MidiPlayerService;

public class PlayerActivity extends AppCompatActivity {
    public static MidiRecording recording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        TextView jsonText = findViewById(R.id.jsonText);
        Bundle extras = getIntent().getExtras();
        if (extras.containsKey("Song")) {
            JSONObject jsonObject = null;
            ArrayList<MidiNote> notes = new ArrayList<>();
            try {
                jsonObject = new JSONObject(extras.getString("Song"));
                JSONArray jsonArray = jsonObject.names();
                for (int i = 1; i < jsonArray.length(); i++) {
                    JSONObject obj = (JSONObject) jsonObject.get((String) jsonArray.get(i));
                    byte type = MidiConstants.MessageTypes.valueOf(obj.getString("Type")).getType();
                    byte channel = (byte) obj.getInt("Channel");
                    byte pitch = (byte) obj.getInt("Pitch");
                    byte velocity = (byte) obj.getInt("Velocity");
                    MidiNote note = new MidiNote(type, channel, pitch, velocity);
                    notes.add(note);
                }
                recording = new MidiRecording();
                recording.setRecordingList(notes);
                // recording.setStartingTimestamp(jsonObject.getLong((String) jsonArray.get(0)));

            } catch (JSONException e) {
                e.printStackTrace();
                finish();
            }
            long duration = recording.getDuration();
            jsonText.setText("Duration: " + duration);
            if (MidiConnectionManager.getInstance().getInputPort() != null || true) {
                Intent startIntent = new Intent(PlayerActivity.this, MidiPlayerService.class);
                startIntent.setAction(Constants.START_PLAYING_RECORDING);
                startService(startIntent);
            }
        }


    }


}
