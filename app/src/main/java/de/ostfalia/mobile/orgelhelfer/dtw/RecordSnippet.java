package de.ostfalia.mobile.orgelhelfer.dtw;

import android.util.SparseArray;

import java.util.ArrayList;

import de.ostfalia.mobile.orgelhelfer.model.MidiEvent;

/**
 * 
 * @author Aaron
 *
 */
public class RecordSnippet {
	private ArrayList<MidiGroup> rec;
	private ArrayList<MidiGroup> currentSnippet;
	int offset;
	private int snippetLength;
	RecordSnippet(ArrayList<MidiGroup> rec, int snippetLength) {
		this.rec = rec;
		this.snippetLength = snippetLength;
		this.currentSnippet = new ArrayList<>();
		for(int i = 0; i < snippetLength && i < rec.size(); i++) {
			currentSnippet.add(rec.get(i));
		}
	}
	
	public void moveSnippet(int movement) {
		offset += movement;
		currentSnippet.clear();
		for(int i = 0; i < snippetLength && i + offset < rec.size(); i++) {
			currentSnippet.add(rec.get(i + offset));
		}
	}
	
	public int size() {
		return currentSnippet.size();
	}
	
	public MidiGroup get(int index) {
		return currentSnippet.get(index);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < currentSnippet.size(); i++) {
			sb.append(currentSnippet.get(i).toString());
			sb.append((char) 9);
		}
		return sb.toString();
	}


	public static SparseArray<ArrayList<MidiEvent>> cutOutZurueckspielEvents(ArrayList<MidiGroup> rec) {
		SparseArray<ArrayList<MidiEvent>> ret = new SparseArray<>();
		for(int i = rec.size() - 1; i >= 0; i--) {
			if(rec.get(i).hasSollZurueckgespieltWerden()) {
				ret.put(i, rec.get(i).cutSollZurueckgespieltWerden());
			}
		}
		return ret;
	}
}
