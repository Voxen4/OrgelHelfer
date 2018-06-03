package de.ostfalia.mobile.orgelhelfer.dtw;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import de.ostfalia.mobile.orgelhelfer.MidiDataManager;
import de.ostfalia.mobile.orgelhelfer.model.MidiEvent;

public class EventScheduler<E extends DtwComparable<E>> implements Runnable {
    public static int REPLAYDELAY = 200;
    ArrayList<E> events;
    int currentIndex;

	long time;

	Dtw dtw;
	Thread t;
	boolean started = false;
	boolean running = false;
	boolean interrupted = false;

	public EventScheduler(Dtw dtw, ArrayList<E> events) {
		this.dtw = dtw;
		this.events = events;
		t = new Thread(this);
	}

	public boolean hasStarted() {
		return started;
	}

	public void start(long time) {
		started = true;
		running = true;
		this.setTime(time);
		t.start();
	}

	public void setTime(long time) {
		this.time = time;
		if(getFollowingIndex(time) <= -1) {
			running = false;
            System.out.println("Songe ended");
        }
        currentIndex = getFollowingIndex(time);
		interrupted = true;
		t.interrupt();
	}

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

	private void sendEvent(E event) {
		if(event instanceof MidiEvent) {
			MidiEvent e = (MidiEvent) event;
            //System.out.print((char) e.getRaw()[1]);
            MidiDataManager.getInstance().sendEvent(e);
        }
	}
}
