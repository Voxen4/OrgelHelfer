package de.ostfalia.mobile.orgelhelfer.activitys;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import de.ostfalia.mobile.orgelhelfer.R;
import de.ostfalia.mobile.orgelhelfer.db.App;
import de.ostfalia.mobile.orgelhelfer.db.Kategorie;
import de.ostfalia.mobile.orgelhelfer.db.MyDatabase;

public class KategorieActivity extends BaseActivity {

    private static long counter = 0;
    public List<Kategorie> data;
    private MyDatabase database;
    private MyAdapter adapter = null;
    private Kategorie temp;



    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_kategorie);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        ImageView addItem = findViewById(R.id.kategorieHinzufügen);

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);




        RecyclerViewSwipeManager swipeMgr = new RecyclerViewSwipeManager();


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
                            database.kategorieDao().delete(temp);
                        }
                    }).start();
                }

            }
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MyAdapter();

        // Erstellen der Datenbank bei Öffnen der Activity
        new Thread(new Runnable() {
            @Override
            public void run() {
                database = App.get().getDB();
                data = database.kategorieDao().getAll();
                for (int i = 0; i < data.size(); i++) {
                    adapter.createnewItem(data.get(i).getName());
                }
            }
        }).start();

        recyclerView.setAdapter(swipeMgr.createWrappedAdapter(adapter));


        swipeMgr.attachRecyclerView(recyclerView);

        // Hinzufügen der Items in das Recyclerview
        addItem.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(KategorieActivity.this);
                final EditText edittext = new EditText(KategorieActivity.this);
                edittext.setMaxLines(1);
                edittext.setInputType(1);


                alert.setTitle(R.string.neue_kategorie);

                alert.setView(edittext);

                alert.setPositiveButton(R.string.hinzufügen, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        adapter.createnewItem(edittext.getText().toString());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                temp = new Kategorie(edittext.getText().toString());
                                data.add(temp);
                                database.kategorieDao().insertOne(temp);
                                // Nochmal holen der Datanbank, damit Einträge direkt gelöscht werden können!
                                data = database.kategorieDao().getAll();
                                App.get().getDB().kategorieDao().getAll();
                            }
                        }).start();

                    }
                });

                alert.setNegativeButton(R.string.verwerfen, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();


            }
        });
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

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            MyItem item = mItems.get(position);
            holder.textView.setText(item.text);
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
            if (result == Swipeable.RESULT_CANCELED) {
                return new SwipeResultActionDefault();
            } else {
                return new MySwipeResultActionRemoveItem(this, position);
            }
        }

        @Override
        public int onGetSwipeReactionType(MyViewHolder holder, int position, int x, int y) {
            return Swipeable.REACTION_CAN_SWIPE_BOTH_H;
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