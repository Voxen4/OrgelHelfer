package de.ostfalia.mobile.orgelhelfer.activitys;
/*
 *    Copyright (C) 2016 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import de.ostfalia.mobile.orgelhelfer.R;
import de.ostfalia.mobile.orgelhelfer.db.App;
import de.ostfalia.mobile.orgelhelfer.db.Kategorie;
import de.ostfalia.mobile.orgelhelfer.db.MyDatabase;
import de.ostfalia.mobile.orgelhelfer.db.Track;
import de.ostfalia.mobile.orgelhelfer.dtw.Dtw;
import de.ostfalia.mobile.orgelhelfer.model.MidiRecording;


public class SongsActivity extends BaseActivity {

    private static long counter = 0;
    private static int kategorieID;
    public List<Kategorie> data;
    public List<Track> dataTrack;
    public boolean flag = true;
    ImageView playTrack;
    private MyDatabase database;
    private MyAdapter adapter = null;
    private Track temp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        System.out.println(extras.getInt("ID"));
        kategorieID = extras.getInt("ID");
        System.out.println("KatID onCreate" + kategorieID);

        setContentView(R.layout.activity_song);

        RecyclerView recyclerView = findViewById(R.id.songsKategorie);
        playTrack = findViewById(R.id.playTrack);
        RecyclerViewSwipeManager expMgr = new RecyclerViewSwipeManager();


        expMgr.setOnItemSwipeEventListener(new RecyclerViewSwipeManager.OnItemSwipeEventListener() {
            public void onItemSwipeStarted(int position) {
            }

            public void onItemSwipeFinished(int position, int result, int afterSwipeReaction) {

                if (result == 4 || result == 2) {
                    temp = dataTrack.get(position);
                    dataTrack.remove(position);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            database.trackDao().delete(temp);
                        }
                    }).start();
                }

            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        adapter = new MyAdapter();

        new Thread(new Runnable() {
            @Override
            public void run() {
                database = App.get().getDB();
                data = database.kategorieDao().getAll();
                dataTrack = database.trackDao().loadAllKategorieTracks(kategorieID);
                System.out.println(data);
                System.out.println(kategorieID);
                for (int i = 0; i < dataTrack.size(); i++) {
                    System.out.println(dataTrack.get(i));
                    adapter.createnewItem(dataTrack.get(i).getTrackTitel());
                }
            }

        }).start();


        recyclerView.setAdapter(expMgr.createWrappedAdapter(adapter));


        expMgr.attachRecyclerView(recyclerView);

    }

    public void playSongMethode(View view) {
        if (ConnectActivity.midiRecording != null) {
            Dtw dtw = Dtw.constructDTW(ConnectActivity.midiRecording.getRecordingList());
        }
    }

    static class MyItem {
        public final long id;
        public final String text;

        public MyItem(long id, String text) {
            this.id = id;
            this.text = text;
        }
    }

    static class MyViewHolder extends AbstractSwipeableItemViewHolder {
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

    static class MyAdapter extends RecyclerView.Adapter<MyViewHolder> implements SwipeableItemAdapter<MyViewHolder> {
        List<MyItem> mItems;

        public MyAdapter() {
            setHasStableIds(true); // this is required for swiping feature.
            mItems = new ArrayList<>();
        }

        public void createnewItem(String kategorieName) {
            mItems.add(new MyItem(counter, kategorieName));
            counter++;

        }

        @Override
        public long getItemId(int position) {
            return mItems.get(position).id; // need to return stable (= not change even after position changed) value
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_for_swipe_minimal, parent, false);
            return new MyViewHolder(v);
        }

        //Ändern des ausgewählten songs
        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            MyItem item = mItems.get(position);
            holder.textView.setText(item.text);
            holder.containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ConnectActivity.midiRecording = MidiRecording.createRecordingFromJson(App.get().getDB().trackDao().loadAllKategorieTracks(kategorieID).get(position).getJsonObject());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();


                }
            });
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public void onSwipeItemStarted(MyViewHolder holder, int position) {
            notifyDataSetChanged();
        }

        @Override
        public SwipeResultAction onSwipeItem(MyViewHolder holder, int position, @SwipeableItemResults int result) {
            if (result == MyAdapter.Swipeable.RESULT_CANCELED) {
                return new SwipeResultActionDefault();
            } else {
                return new MySwipeResultActionRemoveItem(this, position);
            }
        }

        @Override
        public int onGetSwipeReactionType(MyViewHolder holder, int position, int x, int y) {
            return MyAdapter.Swipeable.REACTION_CAN_SWIPE_BOTH_H;
        }

        @Override
        public void onSetSwipeBackground(MyViewHolder holder, int position, @SwipeableItemDrawableTypes int type) {
        }

        interface Swipeable extends SwipeableItemConstants {
        }

        static class MySwipeResultActionRemoveItem extends SwipeResultActionRemoveItem {
            private MyAdapter adapter;
            private int position;


            public MySwipeResultActionRemoveItem(MyAdapter adapter, int position) {
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
