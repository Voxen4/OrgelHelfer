package de.ostfalia.mobile.orgelhelfer.dtw;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author Aaron
 *
 */
public class RecordSnippet {
	ArrayList<MidiList> rec;
	ArrayList<MidiList> currentSnippet;
	int offset;
	int snippetLength;
	public RecordSnippet(ArrayList<MidiList> rec, int snippetLength) {
		this.rec = rec;
		this.snippetLength = snippetLength;
		this.currentSnippet = new ArrayList<MidiList>();
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
	
	public MidiList get(int index) {
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
}
