package de.ostfalia.mobile.orgelhelfer.db;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.annotation.SwipeableItemDrawableTypes;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.annotation.SwipeableItemResults;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.ostfalia.mobile.orgelhelfer.R;
import de.ostfalia.mobile.orgelhelfer.activitys.BaseActivity;
import de.ostfalia.mobile.orgelhelfer.activitys.PlaylistActivity;
import de.ostfalia.mobile.orgelhelfer.model.MidiRecording;
import de.ostfalia.mobile.orgelhelfer.services.Player;


public class Playlist_Tracks extends BaseActivity {


    private static final String LOG_TAG = Playlist_Tracks.class.getSimpleName();
    public static MidiRecording midiRecording;
    private static int counter = 0;
    private static Playlist_Tracks main;
    private static Context context;
    private static ImageView playTrack;
    public List<Track> data;
    private TextView playlistName;
    private MyDatabase database;
    private MyAdapter adapter = null;
    private Track temp;
    private PlaylistActivity.MyItem playlist;
    private List<Track> tracks;
    private String[] tracknames;
    private int playlistUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist__tracks);
        Playlist_Tracks.context = getApplicationContext();

        Bundle dataFromPlaylist = getIntent().getExtras();
        playlist = dataFromPlaylist.getParcelable("playlistName");
        playlistUID = dataFromPlaylist.getInt("id");


        playTrack = findViewById(R.id.playTrack);

        new Thread(new Runnable() {
            @Override
            public void run() {
                tracks = App.get().getDB().trackDao().getAll();
                tracknames = new String[tracks.size()];

            }
        }).start();


        main = Playlist_Tracks.this;
        final RecyclerView recyclerView = findViewById(R.id.recyclerview);
        RecyclerViewSwipeManager swipeMgr = new RecyclerViewSwipeManager();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MyAdapter();

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.setTitle(playlist.text);

        ImageView trackHinzufuegen = findViewById(R.id.trackHinzufügen);
        trackHinzufuegen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRecordings(v);

            }
        });


        //Swipe zum Löschen der Daten!
        swipeMgr.setOnItemSwipeEventListener(new RecyclerViewSwipeManager.OnItemSwipeEventListener() {
            public void onItemSwipeStarted(int position) {
            }

            public void onItemSwipeFinished(int position, int result, int afterSwipeReaction) {

                if (result == 4 || result == 2) {
                    temp = data.get(position);
                    data.remove(position);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            database.trackDao().delete(temp);
                        }
                    }).start();
                }

            }
        });


        // Erstellen der Datenbank bei Öffnen der Activity
        new Thread(new Runnable() {
            @Override
            public void run() {
                database = App.get().getDB();
                data = database.trackDao().loadAllPlaylistTracks(playlistUID);
                for (int i = 0; i < data.size(); i++) {
                    try {
                        adapter.createnewItem(data.get(i).getTrackTitel(), data.get(i).getJsonObject());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        recyclerView.setAdapter(swipeMgr.createWrappedAdapter(adapter));
        swipeMgr.attachRecyclerView(recyclerView);

        if (midiRecording != null) {
            playTrack.setEnabled(true);
        }
    }

    public void loadRecordings(View view) {


        AlertDialog.Builder builder = new AlertDialog.Builder(Playlist_Tracks.this);

        final boolean[] checkedColors = new boolean[tracks.size()];

        for (int i = 0; i < tracks.size(); i++) {
            tracknames[i] = tracks.get(i).getTrackTitel();
        }


        builder.setMultiChoiceItems(tracknames, checkedColors, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                // Update the current focused item's checked status
                checkedColors[which] = isChecked;
            }
        });

        // Set a title for alert dialog
        builder.setTitle("Lieder in die Playlist hinzufügen?");

        // Set the positive/yes button click listener
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when click positive button
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < checkedColors.length; i++) {
                            boolean checked = checkedColors[i];
                            if (checked) {
                                temp = tracks.get(i);
                                temp.setPlaylistFremdschluessel(playlistUID);
                                database.trackDao().updateOne(temp);
                            }
                        }
                    }
                }).start();
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do something when click the neutral button
            }
        });

        AlertDialog dialog = builder.create();
        // Display the alert dialog on interface
        dialog.show();


    }


    public void loadRecordings1(View view) {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(Environment.getExternalStorageDirectory().getPath() + "/OrgelHelfer");
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"json"};
        final FilePickerDialog dialog = new FilePickerDialog(this, properties);
        dialog.setTitle("Datei auswählen");
        dialog.show();
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                final File file = new File(files[0]);
                JSONObject jsonObject = null;
                if (file.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        StringBuilder jsonString = new StringBuilder();
                        while (reader.ready()) {
                            String line = reader.readLine();
                            jsonString.append(line);
                        }
                        jsonObject = new JSONObject(jsonString.toString());
                    } catch (IOException e) {
                        Log.d(LOG_TAG, "Error Loading Recording: " + e.toString());
                        e.printStackTrace();
                    } catch (JSONException e) {
                        Log.d(LOG_TAG, "Error Loading Recording Wrong Json Format: " + e.toString());
                        e.printStackTrace();
                    }

                }
                adapter.createnewItem(file.getName().substring(0, file.getName().lastIndexOf(46)), jsonObject);
                final JSONObject finalJsonObject = jsonObject;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        temp = new Track(file.getName().substring(0, file.getName().lastIndexOf(46)), playlist.text, finalJsonObject.toString());
                        data.add(temp);
                        database.trackDao().insertOne(temp);
                        // Nochmal holen der Datanbank, damit Einträge direkt gelöscht werden können!
                        data = database.trackDao().getAllTracks(playlist.text);

                    }
                }).start();


            }
        });

    }


    static class MyItem {
        public final long id;
        public final String text;
        private final JSONObject jsonObject;

        public MyItem(long id, String text, JSONObject jsonObject) {
            this.id = id;
            this.text = text;
            this.jsonObject = jsonObject;


        }
    }


    static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements SwipeableItemAdapter<MyAdapter.MyViewHolder> {
        List<MyItem> mItems;
        private int selectedPos = RecyclerView.NO_POSITION;

        public MyAdapter() {
            setHasStableIds(true); // this is required for swiping feature.
            mItems = new ArrayList<>();
        }

        public void createnewItem(String trackTitle, JSONObject jsonObject) {

            mItems.add(new MyItem(counter, trackTitle, jsonObject));
            counter++;
            main.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });

        }

        @Override
        public long getItemId(int position) {
            return mItems.get(position).id; // need to return stable (= not change even after position changed) value
        }

        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_for_swipe_minimal, parent, false);
            return new MyAdapter.MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final MyAdapter.MyViewHolder holder, final int position) {
            final MyItem item = mItems.get(position);
            holder.textView.setText(item.text);
            holder.itemView.setSelected(selectedPos == position);
            holder.itemView.setBackgroundResource(selectedPos == position ? R.drawable.markeditem : Color.TRANSPARENT);

            holder.containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) return;

                    notifyItemChanged(selectedPos);
                    selectedPos = holder.getLayoutPosition();
                    notifyItemChanged(selectedPos);
                    setRecording(item.jsonObject);

                }
            });


        }

        private void setRecording(JSONObject recordingJson) {
            midiRecording = MidiRecording.createRecordingFromJson(recordingJson);
            playTrack.setEnabled(true);
            Player.setIsRecordingPlaying(false);

            playTrack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Player.IsRecordingPlaying()) {
                        Player.setIsRecordingPlaying(false);
                        main.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playTrack.setImageResource(R.mipmap.play);
                            }
                        });
                    } else {
                        Player.playRecording(midiRecording, null);
                        main.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playTrack.setImageResource(R.mipmap.pausebutton);
                            }
                        });
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public void onSwipeItemStarted(MyAdapter.MyViewHolder holder, int position) {
            notifyDataSetChanged();
        }

        @Override
        public SwipeResultAction onSwipeItem(MyAdapter.MyViewHolder holder, int position, @SwipeableItemResults int result) {
            if (result == Playlist_Tracks.MyAdapter.Swipeable.RESULT_CANCELED) {
                return new SwipeResultActionDefault();
            } else {
                return new Playlist_Tracks.MyAdapter.MySwipeResultActionRemoveItem(this, position);
            }
        }

        @Override
        public int onGetSwipeReactionType(MyAdapter.MyViewHolder holder, int position, int x, int y) {
            return Playlist_Tracks.MyAdapter.Swipeable.REACTION_CAN_SWIPE_BOTH_H;
        }

        @Override
        public void onSetSwipeBackground(MyAdapter.MyViewHolder holder, int position, @SwipeableItemDrawableTypes int type) {
        }

        interface Swipeable extends SwipeableItemConstants {
        }

        static class MySwipeResultActionRemoveItem extends SwipeResultActionRemoveItem {
            private Playlist_Tracks.MyAdapter adapter;
            private int position;


            public MySwipeResultActionRemoveItem(Playlist_Tracks.MyAdapter adapter, int position) {
                this.adapter = adapter;
                this.position = position;

            }

            @Override
            protected void onPerformAction() {
                adapter.mItems.remove(position);
                adapter.notifyItemRemoved(position);

            }
        }

        class MyViewHolder extends AbstractSwipeableItemViewHolder {
            FrameLayout containerView;
            TextView textView;

            public MyViewHolder(View itemView) {
                super(itemView);
                containerView = itemView.findViewById(R.id.container);
                textView = itemView.findViewById(android.R.id.text1);
                textView.setTextSize(25);

                textView.setTextAppearance(R.style.textstyle);
            }

            @Override
            public View getSwipeableContainerView() {
                return containerView;
            }
        }
    }


}
