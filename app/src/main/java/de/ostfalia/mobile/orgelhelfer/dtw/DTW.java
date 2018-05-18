package de.ostfalia.mobile.orgelhelfer.dtw;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import de.ostfalia.mobile.orgelhelfer.MidiDataManager;
import de.ostfalia.mobile.orgelhelfer.midi.MidiConstants;
import de.ostfalia.mobile.orgelhelfer.model.MidiEvent;

/**
 * DTW Algorithmus.
 * @author Aaron
 *
 */
public class DTW implements Runnable{
	public static int STREAMBUFFERLENGTH = 20;
	public static int RECORDINGSNIPPETLENGTH = 10;
	public static int THREADSLEEPTIME = 100;
	public static byte BEFEHLE = MidiConstants.MessageTypes.STATUS_CONTROL_CHANGE.getType();

	boolean running;
	
	StreamBuffer streamBuffer;
	RecordSnippet recordSnippet;
	HashMap<Integer, ArrayList<MidiEvent>> befehleHashMap;
	float[][] matrix;
	
	public DTW(ArrayList<MidiList> rec) {
		streamBuffer = new StreamBuffer(STREAMBUFFERLENGTH);
		recordSnippet = new RecordSnippet(rec, RECORDINGSNIPPETLENGTH);
		matrix = new float[STREAMBUFFERLENGTH][RECORDINGSNIPPETLENGTH];
		befehleHashMap = new HashMap<Integer, ArrayList<MidiEvent>>();
		for(int i = rec.size(); i >= 0; i--) {
			if (rec.get(i).hasMidiOfType(BEFEHLE)) {
				befehleHashMap.put(i, rec.get(i).getMidiOfType(BEFEHLE));
				rec.remove(i);
			}
		}
	}
	
	public void stop() {
		running = false;
	}
	
	MidiList nextEvents = new MidiList();
	public synchronized void addEvent(MidiEvent event) {
		nextEvents.addEvent(event);
	}
	
	private synchronized MidiList getNextEvents() {
		MidiList ret = nextEvents.clone();
		nextEvents.clear();
		return ret;
	}
	
	int lastMinIndex;
	int currentMinIndex;
	int totalStreamedElements;
	public void next() {
		MidiList e = getNextEvents();
		totalStreamedElements++;
		streamBuffer.addElement(e);
		for(int i = 0; i < recordSnippet.size(); i++) {
			//matrix[streamBuffer.getCurrentIndex()][i] = Math.abs(e.getValue() - recordSnippet.getIndex(i).getValue()) + min(neighboursOf(streamBuffer.getCurrentIndex(),i));
			matrix[streamBuffer.getCurrentIndex()][i] = MidiList.getDist(e, recordSnippet.get(i)) + min(neighboursOf(streamBuffer.getCurrentIndex(),i));
			//matrix[streamBuffer.getCurrentIndex()][i] = Math.abs(e.getValue() - recordSnippet.getIndex(i).getValue());
		}
		
		if(totalStreamedElements > STREAMBUFFERLENGTH / 2) {
			adjustSpread();
		}
		// Suche nach allen Befehlen die den Status BEFEHL haben zwischen dm letzten minimum und dem jetzigen Minimum
		int i = lastMinIndex;
		int j = 0;
		if(lastMinIndex < currentMinIndex) {
			while (i != currentMinIndex && j <= recordSnippet.size()) {
				if (befehleHashMap.get(i + j) != null) {
					for (MidiEvent event : befehleHashMap.get(i + j)) {
						MidiDataManager.getInstance().sendEvent(event);
					}
				}
				j++;
			}
			lastMinIndex = currentMinIndex;
		}

	}
	
	private ArrayList<Float> neighboursOf(int x, int y) {
		ArrayList<Float> neighbours = new ArrayList<Float>(4);
		
		if(x > 0 && x != streamBuffer.getCurrentIndex() + 1) {
			neighbours.add(matrix[x - 1][y]);
			if(y > 0) {
				neighbours.add(matrix[x - 1][y - 1]);
			}
		} else if(x == 0 && totalStreamedElements > STREAMBUFFERLENGTH) {
			neighbours.add(matrix[matrix.length - 1][y]);
			if(y > 0) {
				neighbours.add(matrix[matrix.length - 1][y - 1]);
			}
		}
		
		if(y > 0) {
			neighbours.add(matrix[x][y - 1]);
		}
		return neighbours;
	}
	
	private int getMinIndex(int col) {
		float value = Integer.MAX_VALUE;
		int index = -1;
		for(int i = 0; i < matrix[col].length; i++) {
			if(matrix[col][i] <= value) {
				value = matrix[col][i];
				index = i;
			}
		}
		return index;
	}
	
	
	
	
	
	private float min(ArrayList<Float> list) {
		float min = Float.MAX_VALUE;
		if(list.size() == 0) {
			return 0;
		} else {
			for(Float f: list) {
				if(min > f) {
					min = f;
				}
			}
		}
		return min;
	}
	
	private void adjustSpread() {
		float min = Integer.MAX_VALUE;
		int minIndex = 0;
		int spread = 0;
		for(int i = 0; i < recordSnippet.size(); i++) {
			if(min > matrix[streamBuffer.getCurrentIndex()][i]) {
				min = matrix[streamBuffer.getCurrentIndex()][i];
				minIndex = i;
			}else if(min == matrix[streamBuffer.getCurrentIndex()][i]) {
				spread = Math.abs(minIndex - i);
			}
		}
		
		int deviation = minIndex - RECORDINGSNIPPETLENGTH / 2;
		
		if(deviation > RECORDINGSNIPPETLENGTH / 4) {
			deviateMatrix(deviation);
		}
	}
	
	private void deviateMatrix(int deviation) {
		Log.d("DTW", "------------MATRIX VERSCHOBEN UM: " + deviation + "------------");
		Log.d("DTW", this.toString());
		recordSnippet.moveSnippet(deviation);
		float[][] newMatrix = matrix.clone();
		for(int i = 0; i < streamBuffer.size(); i++) {
			for(int n = 0; n + deviation < recordSnippet.size(); n++) {
				newMatrix[i][n] = matrix[i][n + deviation];
			}
			for(int n = recordSnippet.size() - deviation; n < recordSnippet.size(); n++) {
				newMatrix[i][n] = MidiList.getDist(streamBuffer.get(i), recordSnippet.get(n)) + min(neighboursOf(i,n));
			}
		}
		this.matrix = newMatrix;
		Log.d("DTW","\n");
		Log.d("DTW",this.toString());
		Log.d("DTW","-----------------------------------------------------------------");
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append((char) 9);
		sb.append(streamBuffer.toString());
		sb.append("\n");
		for(int i = 0; i < recordSnippet.size(); i++) {
			sb.append(recordSnippet.get(i).toString());
			sb.append((char) 9);
			for(int n = 0; n < streamBuffer.size(); n++) {
				sb.append(matrix[n][i]);
				sb.append((char) 9);
				if(n == streamBuffer.getCurrentIndex()) {
					sb.append("|");
					sb.append((char) 9);
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public void run() {
		running = true;
		Log.d("DTW","Thread started.");
		Log.d("DTW","Configs: Thread sleep time " + THREADSLEEPTIME);
		Log.d("DTW","Record snippet length: " + RECORDINGSNIPPETLENGTH);
		Log.d("DTW", "Stream buffer length: " + STREAMBUFFERLENGTH);
		long time = System.nanoTime();
		while(running) {
			time = System.nanoTime() - time;
			Log.d("DTW","Thread slept " + time / 1000000f + " milli seconds");
			time = System.nanoTime();
			next();
			Log.d("DTW","finished after " + (System.nanoTime() - time) / 1000000f + " milli seconds");
			time = System.nanoTime();
			Log.d("DTW",this.toString());
		}
		try {
			Thread.sleep(THREADSLEEPTIME);
		} catch (InterruptedException i) {
			i.printStackTrace();
		}
	}
}
