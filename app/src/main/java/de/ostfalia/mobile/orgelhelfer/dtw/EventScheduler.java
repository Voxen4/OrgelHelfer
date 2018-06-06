package de.ostfalia.mobile.orgelhelfer.dtw;


import java.util.ArrayList;

import de.ostfalia.mobile.orgelhelfer.MidiDataManager;
import de.ostfalia.mobile.orgelhelfer.model.MidiEvent;

public class EventScheduler<E extends DtwComparable<E>> implements Runnable {
    private static int REPLAYDELAY = 200;
	private ArrayList<E> events;
	private int currentIndex;

	private long time;

	private Dtw dtw;
	private Thread t;
	private boolean started = false;
	private boolean running = false;
	private boolean interrupted = false;

	EventScheduler(Dtw dtw, ArrayList<E> events) {
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
