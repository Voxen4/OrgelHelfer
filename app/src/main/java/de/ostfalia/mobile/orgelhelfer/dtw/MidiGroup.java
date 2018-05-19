package de.ostfalia.mobile.orgelhelfer.dtw;

import java.util.ArrayList;
import java.util.Collections;

import de.ostfalia.mobile.orgelhelfer.model.MidiEvent;

/**
 * Datenstruktur einer Midi-Liste.
 * Macht eigentlich das selbe wie eine ArrayList<MidiEvent> au�er dass sie zus�tzlich immer sortiert ist.
 * Zudem kann die Anzahl unterschiedlicher Objekte von zwei MidiLists durch getDist errechnet werden.
 * @author Aaron
 *
 */
public class MidiGroup implements Cloneable{
	private ArrayList<MidiEvent> events;
	public MidiGroup() {
		events = new ArrayList<MidiEvent>();
	}
	
	public MidiGroup(ArrayList<MidiEvent> events) {
		this.events = events;
		Collections.sort(this.events);
	}

	/*@Deprecated
	public MidiGroup(int...ints) {
		this.events = new ArrayList<MidiEvent>();
		for(int i: ints) {
			//Keine probedaten verfügbar
			//events.add(new MidiEvent((byte) i));
		}
		Collections.sort(this.events);
	}*/

	/**
	 * Gibt an, ob sich in dieser MidiGroup ein Element befindet, dass zurückgespielt werden soll.
	 * @return true, falls midnestens ein Element existiert, dass zurückgespielt werden soll, und false falls keins existiert.
	 */
	public boolean hasSollZurueckgespieltWerden() {
		for(MidiEvent event: events) {
			if(event.getSollZurueckspielenWerden()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gibt alle Elemente als ArrayList<MidiEvent> zurück, die zurückgepsielt werden sollen.
	 * @return : ArrayList<MidiEvent> mit der eigentschaft event.getSollZurueckspielenWerden() == true
	 */
	public ArrayList<MidiEvent> getSollZurueckgespieltWerden() {
		ArrayList<MidiEvent> zurueckgespielteEvents = new ArrayList<MidiEvent>();
		for(MidiEvent event: events) {
			if(event.getSollZurueckspielenWerden()) {
				zurueckgespielteEvents.add(event);
			}
		}
		return zurueckgespielteEvents;
	}

	public ArrayList<MidiEvent> cutSollZurueckgespieltWerden() {
		ArrayList<MidiEvent> zurueckgespielteEvents = new ArrayList<MidiEvent>();
		for(MidiEvent event: events) {
			if(event.getSollZurueckspielenWerden()) {
				zurueckgespielteEvents.add(event);
				events.remove(event);
			}
		}
		return zurueckgespielteEvents;
	}
	
	/**
	 * F�gt der MidiGroup ein neues MidiEvent event hinzu.
	 * Wenn schon ein identisches Element in der Liste vorhanden ist, wird es nicht hinzugef�gt.
	 * Die Komplexit�t betr�gt O(log(n)) wobei n = events.size() ist.
	 * @param event : Das hinzuf�gende Element
	 */
	public void addEvent(MidiEvent event) {
		if(Collections.binarySearch(events, event) < 0) {
			events.add(event);
		}
	}
	
	@SuppressWarnings("unchecked")
	public MidiGroup clone() {
		return new MidiGroup((ArrayList<MidiEvent>) events.clone());
	}
	
	public void clear() {
		events.clear();
	}

	
	/**
	 * Errechnet die Distanz zwischen zwei MidiLists. Die implementation erfolgt �ber zwei bin�re Suchen.
	 * Die Komplexit�t betr�gt: O(log(n) + log(m)) ~ O(log(n)) wobei n = midiCache.events.size und m = midiCache2.events.size
	 * @param midiCache1 : Liste 1
	 * @param midiCache2 : Liste 2
	 * @return |R| = (A U B) / A , wobei A und B Mengen von MidiEvents aus den Objekten der MidiListen sind.
	 */
	
	public static float getDist(MidiGroup midiCache1, MidiGroup midiCache2) {
		float dist = 0;
		for(MidiEvent eventA: midiCache1.events) {
			if(Collections.binarySearch(midiCache2.events, eventA) < 0) {
				dist++;
			}
		}
		for(MidiEvent eventB: midiCache2.events) {
			if(Collections.binarySearch(midiCache1.events, eventB) < 0) {
				dist++;
			}
		}
		return dist;
	}
	
	@Override
	public String toString() {
		return events.toString();
	}
}
