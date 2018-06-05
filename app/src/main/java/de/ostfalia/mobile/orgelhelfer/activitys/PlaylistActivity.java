package de.ostfalia.mobile.orgelhelfer.activitys;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.annotation.SwipeableItemDrawableTypes;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.annotation.SwipeableItemResults;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;

import java.util.ArrayList;
import java.util.List;

import de.ostfalia.mobile.orgelhelfer.db.Playlist_Tracks;
import de.ostfalia.mobile.orgelhelfer.R;
import de.ostfalia.mobile.orgelhelfer.db.App;
import de.ostfalia.mobile.orgelhelfer.db.MyDatabase;
import de.ostfalia.mobile.orgelhelfer.db.Playlist;

public class PlaylistActivity extends AppCompatActivity {

    private static final String LOG_TAG = PlaylistActivity.class.getSimpleName();
    private MyDatabase database;
    private static int counter = 0;
    private PlaylistActivity.MyAdapter adapter = null;
    public static List<Playlist> playlistData;
    private ImageView erstellen;

    public Playlist playlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);


        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        RecyclerViewSwipeManager swipeMgr = new RecyclerViewSwipeManager();

        erstellen = (findViewById(R.id.playlistHinzufügen));

        //Swipe zum Löschen der Daten!
        swipeMgr.setOnItemSwipeEventListener(new RecyclerViewSwipeManager.OnItemSwipeEventListener() {
            public void onItemSwipeStarted(int position) {
            }

            public void onItemSwipeFinished(int position, int result, int afterSwipeReaction) {

                if (result == 4 || result == 2) {
                    playlist = playlistData.get(position);

                    playlistData.remove(position);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            database.playlistDao().delete(playlist);
                        }
                    }).start();
                }

            }
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PlaylistActivity.MyAdapter();



        // Erstellen der Datenbank bei Öffnen der Activity
        new Thread(new Runnable() {
            @Override
            public void run() {
                database = App.get().getDB();

                playlistData = database.playlistDao().getAll();
                for (int i = 0; i < playlistData.size(); i++) {
                    adapter.createnewItem(playlistData.get(i).getName());
                }

            }
        }).start();

        recyclerView.setAdapter(swipeMgr.createWrappedAdapter(adapter));


        swipeMgr.attachRecyclerView(recyclerView);

        erstellen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder alert = new AlertDialog.Builder(PlaylistActivity.this);
                final EditText edittext = new EditText(PlaylistActivity.this);
                edittext.setMaxLines(1);
                edittext.setInputType(1);


                alert.setTitle("Neue Playlist:");

                alert.setView(edittext);

                alert.setPositiveButton("Hinzufügen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        adapter.createnewItem(edittext.getText().toString());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                playlist = new Playlist(edittext.getText().toString());
                                playlistData.add(playlist);
                                database.playlistDao().insertOne(playlist);
                                // Nochmal holen der Datanbank, damit Einträge direkt gelöscht werden können!
                                playlistData = database.playlistDao().getAll();
                            }
                        }).start();


                    }
                });

                alert.setNegativeButton("Verwerfen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();

            }
        });

    }

    public static class MyItem implements Parcelable {
        public final int id;
        public final String text;

        public MyItem(int id, String text) {
            this.id = id;
            this.text = text;
        }

        public MyItem (Parcel in) {
            id = in.readInt();
            text = in.readString();
        }
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeString(text);

        }

        public static final Parcelable.Creator<MyItem> CREATOR = new Parcelable.Creator<MyItem>() {

            public MyItem createFromParcel(Parcel in) {
                return new MyItem(in);
            }

            public MyItem[] newArray(int size) {
                return new MyItem[size];
            }
        };
    }

    static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements SwipeableItemAdapter<MyAdapter.MyViewHolder> {
        interface Swipeable extends SwipeableItemConstants {
        }

        static class MyViewHolder extends AbstractSwipeableItemViewHolder{
            FrameLayout containerView;
            TextView textView;

            public MyViewHolder(View itemView) {
                super(itemView);

                containerView = itemView.findViewById(R.id.container);
                textView = itemView.findViewById(android.R.id.text1);
                textView.setTextSize(25);

                textView.setTextAppearance(R.style.fontForNotificationLandingPage);

            }

            @Override
            public View getSwipeableContainerView() {
                return containerView;
            }


        }
        List<MyItem> mItems;

        public MyAdapter() {
            setHasStableIds(true); // this is required for swiping feature.
            mItems = new ArrayList<>();
        }

        public void createnewItem(String playlistName) {
            mItems.add(new MyItem(counter, playlistName));
            counter++;

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
        public void onBindViewHolder(MyAdapter.MyViewHolder holder, int position) {
            final MyItem item = mItems.get(position);

            holder.textView.setText(item.text);


            holder.containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), Playlist_Tracks.class);
                    intent.putExtra("playlistName", item);
                    v.getContext().startActivity(intent);

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
            if (result == PlaylistActivity.MyAdapter.Swipeable.RESULT_CANCELED) {
                return new SwipeResultActionDefault();
            } else {
                return new PlaylistActivity.MyAdapter.MySwipeResultActionRemoveItem(this, position);
            }
        }

        @Override
        public int onGetSwipeReactionType(MyAdapter.MyViewHolder holder, int position, int x, int y) {
            return PlaylistActivity.MyAdapter.Swipeable.REACTION_CAN_SWIPE_BOTH_H;
        }

        @Override
        public void onSetSwipeBackground(MyAdapter.MyViewHolder holder, int position, @SwipeableItemDrawableTypes int type) {
        }

        static class MySwipeResultActionRemoveItem extends SwipeResultActionRemoveItem {
            private PlaylistActivity.MyAdapter adapter;
            private int position;


            public MySwipeResultActionRemoveItem(PlaylistActivity.MyAdapter adapter, int position) {
                this.adapter = adapter;
                this.position = position;

            }

            @Override
            protected void onPerformAction() {
                adapter.mItems.remove(position);
                adapter.notifyItemRemoved(position);

            }
        }
    }
}