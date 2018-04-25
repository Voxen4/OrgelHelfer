package de.ostfalia.mobile.orgelhelfer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class MidiEventArrayAdapter extends ArrayAdapter<MidiEvent> {
    private Activity activity;
    private List<MidiEvent> event;
    private static LayoutInflater inflater = null;

    public MidiEventArrayAdapter(Activity activity, int textViewResourceId, List<MidiEvent> _MidiEvent) {
        super(activity, textViewResourceId, _MidiEvent);
        this.activity = activity;
        this.event = _MidiEvent;

        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public int getCount() {
        return event.size();
    }

    public MidiEvent getItem(int position) {
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
        vi = (TextView) vi.findViewById(R.id.text1);
        ((TextView) vi).setText(getItem(position).toString());

        return vi;
    }
}