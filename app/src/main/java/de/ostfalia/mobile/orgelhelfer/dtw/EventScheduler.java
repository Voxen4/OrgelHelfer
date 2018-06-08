package de.ostfalia.mobile.orgelhelfer.dtw;


import android.util.Log;

import java.util.ArrayList;

import de.ostfalia.mobile.orgelhelfer.MidiDataManager;
import de.ostfalia.mobile.orgelhelfer.model.MidiEvent;

/**
 * Der EventScheduler ist ein Objekt dass sich um das zeitlich richtige Absenden von Events kümmert.
 * Alle Events die von diesem Objekt gesendet werden, werden außerdem zurück an das Dtw-Objekt gesendet um einen erneuten Einstieg
 * ermöglichen zu können.
 * @param <E> : Eine Klasse die das Interface DtwComparable implemeniert.
 */
public class EventScheduler<E extends DtwComparable<E>> implements Runnable {
    private static final String LOG_TAG = EventScheduler.class.getSimpleName();
    private static int REPLAYDELAY = 200;
    private ArrayList<E> events;
	private int currentIndex;

	private long time;

	private Dtw dtw;
	private Thread t;
	private boolean started = false;
	private boolean running = false;
	private boolean interrupted = false;

	/**
	 * Erstellt ein neues Objekt des Typs EventScheduler.
	 * @param dtw : Das Dtw-Objekt, an den die gesendeten Daten übermittelt werden um die neuste Position zu errechnen.
	 * @param events : Die Liste der Events die abgepielt werden sollen (wenn sie nicht übersprungen werden)
	 */
	EventScheduler(Dtw dtw, ArrayList<E> events) {
		this.dtw = dtw;
		this.events = events;
		t = new Thread(this);
	}

	/**
	 * Gibt zurück, ob der EventScheduler gestartet wurde.
	 * @return
	 */
	public boolean hasStarted() {
		return started;
	}

	public void start(long time) {
		started = true;
		running = true;
		this.setTime(time);
		t.start();
	}

	/**
	 * Setzt eine neue Zeit für den EventScheduler.
	 * @param time . Der Zeitpunkt an dem das letzte MidiEvent vom Benutzer empfangen wurde.
	 */
	public void setTime(long time) {
		this.time = time;
		if(getFollowingIndex(time) <= -1) {
			running = false;
            Log.d(LOG_TAG, "Songe ended");
        }
        currentIndex = getFollowingIndex(time);
		interrupted = true;
		t.interrupt();
	}

	/**
	 * Gibt den Index des nächsten Elements das gespielt werden soll zurück.
	 * @param time :
	 * @return das erste Element aus der Liste events für das gilt event.getTimestamp() > time
	 */
	private int getFollowingIndex(long time) {
		int i = 0;
		while(i < events.size() && events.get(i).getTimestamp() <= time) {
			i++;
		}//i >= events.size() || events.get(i).getTimestamp() >= time
		if(i >= events.size()) {
			return -1;
		} else {
			return i;
		}
	}

	@Override
	public void run() {
		while(running && currentIndex < events.size()) {
			try {
				if(!interrupted) {
					Thread.sleep(events.get(currentIndex).getTimestamp() - time);
				} else {
					interrupted = false;
					Thread.sleep(events.get(currentIndex).getTimestamp() - time + REPLAYDELAY);
				}
				sendEvent(events.get(currentIndex));
				dtw.next((MidiEvent) events.get(currentIndex));
				this.time = events.get(currentIndex).getTimestamp();
				currentIndex++;
			} catch (InterruptedException e) {
			}
		}
        System.out.println("Song ended");
    }

	/**
	 * Sendent ein Event e (MidiEvent) an den MidiDataManager
	 * @param event : Das Event dass vom Gerät abgespielt werden soll.
	 */
	private void sendEvent(E event) {
		if(event instanceof MidiEvent) {
			MidiEvent e = (MidiEvent) event;
            //System.out.print((char) e.getRaw()[1]);
            MidiDataManager.getInstance().sendEvent(e);
        }
	}
}
