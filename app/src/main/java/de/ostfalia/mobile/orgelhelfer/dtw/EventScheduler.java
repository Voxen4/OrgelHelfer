package de.ostfalia.mobile.orgelhelfer.dtw;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import de.ostfalia.mobile.orgelhelfer.MidiDataManager;
import de.ostfalia.mobile.orgelhelfer.model.MidiEvent;
import de.ostfalia.mobile.orgelhelfer.model.MidiProgram;

public class EventScheduler<E extends DtwComparable<E>> implements Runnable {
	private static final String LOG_TAG = EventScheduler.class.getSimpleName();
	public boolean started = false;
	ArrayList<E> events;
	int currentIndex;
	long time;
	Thread t = new Thread(this);
	boolean running = false;
	
	public EventScheduler(ArrayList<E> events) {
		t = new Thread(this);
		this.events = events;
		Collections.sort(events);
		currentIndex = 0;
	}
	
	public void start(long time) {
		running = true;
		started = true;
		this.time = time;
		//|time - startingTime| sollte immer gleich bleiben.
		t.start();
	}
	
	public boolean hasStarted() {
		return started;
	}
	
	public void stop() {
		running = false;
	}
	
	public void updateTime(long time) {
		this.time = time;
		t.interrupt();
	}

	@Override
	public void run() {
		long eventDeltaTime;
		long systemMilliTime, systemDeltaTime;
		Iterator<E> it = events.iterator();
		E currentEvent = it.next();
		while(running) {
			eventDeltaTime = currentEvent.getTimestamp() - time;
			systemMilliTime = System.nanoTime() / 1000000;
			try {
				if(eventDeltaTime > 20) {
					//System.out.println("Thread will sleep for " + eventDeltaTime);
					Thread.sleep(eventDeltaTime);
				}
				systemDeltaTime = System.nanoTime() / 1000000 - systemMilliTime;	
				//System.out.println("Thread selpt for " + systemDeltaTime);
				time = time + systemDeltaTime;
				while (currentEvent != null && time > currentEvent.getTimestamp() - 50) {
					sendEvent(currentEvent);
					if(it.hasNext()) {
						currentEvent = it.next();
					} else {
						currentEvent = null;
					}
				}
				if(currentEvent == null) {
					running = false;
					//System.out.println("All Events send. Stopping Thread.");
				}
				
			} catch (InterruptedException e) {
			}
			
		}
	}

	private void sendEvent(E currentEvent) {
		if(currentEvent instanceof MidiEvent){
			MidiEvent e = (MidiEvent) currentEvent;
			//MidiDataManager.getInstance().sendEvent(MidiProgram.ProgramTest);
			MidiDataManager.getInstance().sendEvent(e);
			//Log.d(LOG_TAG,"Sending Event: " + e.toString());
		}
	}
}
