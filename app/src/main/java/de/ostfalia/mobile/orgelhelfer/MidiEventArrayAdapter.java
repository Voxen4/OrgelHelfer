package de.ostfalia.mobile.orgelhelfer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class MidiEventArrayAdapter extends ArrayAdapter<MidiNote> {
    private static LayoutInflater inflater = null;
    private Activity activity;
    private List<MidiNote> event;

    public MidiEventArrayAdapter(Activity activity, int textViewResourceId, List<MidiNote> _MidiNote) {
        super(activity, textViewResourceId, _MidiNote);
        this.activity = activity;
        this.event = _MidiNote;

        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public int getCount() {
        return event.size();
    }

    public MidiNote getItem(int position) {
        return event.get(position);
    }

    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.simple_list_item_slim, null);
        }
        vi = vi.findViewById(R.id.text1);
        ((TextView) vi).setText(getItem(position).toString());
        return vi;
    }
}