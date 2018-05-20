package de.ostfalia.mobile.orgelhelfer.dtw;

import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;

import de.ostfalia.mobile.orgelhelfer.MidiDataManager;
import de.ostfalia.mobile.orgelhelfer.model.MidiEvent;

/**
 * DTW-Algorithmus. Dieser Algorithmus berrechnet den Effizientesten Pfad zwischen zwei Folgen (in diesem Fall Folgen von MidiGroups).
 * Eine MidiGroup besteht aus keinem, einem oder mehreren MidiEvents die innerhalb von THREADSLEEPTIME Millisekunden an den Algorithmus gegeben wurden.
 * @author Aaron
 *
 */
public class DTW implements Runnable{
	private static final int STREAMBUFFERLENGTH = 20;
	private static final int RECORDINGSNIPPETLENGTH = 10;
	private static final int THREADSLEEPTIME = 100;

	private StreamBuffer streamBuffer;
	private RecordSnippet recordSnippet;
	private float[][] matrix;
	private SparseArray<ArrayList<MidiEvent>> befehleSparseArray;

	private int lastMinIndex;
	private int totalStreamedGroups;

	public DTW(ArrayList<MidiGroup> rec) {
		streamBuffer = new StreamBuffer(STREAMBUFFERLENGTH);

		//recordSnippet = new RecordSnippet(rec, RECORDINGSNIPPETLENGTH);
		//matrix = new float[STREAMBUFFERLENGTH][RECORDINGSNIPPETLENGTH];

		/*befehleSparseArray = new SparseArray<>();
		for(int i = rec.size(); i >= 0; i--) {
			if (rec.get(i).hasSollZurueckgespieltWerden()) {
				befehleSparseArray.put(i, rec.get(i).getSollZurueckgespieltWerden());
				rec.remove(i);
			}
		}*/

		befehleSparseArray = RecordSnippet.cutOutZurueckspielEvents(rec);
		recordSnippet = new RecordSnippet(rec, RECORDINGSNIPPETLENGTH);
		matrix = new float[STREAMBUFFERLENGTH][RECORDINGSNIPPETLENGTH];
	}

	private boolean running;
	public void stop() {
		running = false;
	}
	
	private MidiGroup nextEvents = new MidiGroup();
	public synchronized void addEvent(MidiEvent event) {
		nextEvents.addEvent(event);
	}
	
	private synchronized MidiGroup getNextEvents() {
		MidiGroup ret = nextEvents.clone();
		nextEvents.clear();
		return ret;
	}

	/**
	 * Aufruf dieser Methode Erfolgt ausschließlich über die run-Methode.
	 * Berechnet die Matrix mit allen Events die zwisachen dem letzten Aufruf und jetzt durch die Methode addEvent() hinzugefügt wurden. (pushedEventGroup im folgenden)
	 * Abfolge: 1. Berrechnung der Distanz zwischen dem pushedEventGroup und allen anderen EventGroups in einem Intervall um das jetzige  Minimum der Aufnahme.
	 * 			2. Ermittlung des neuen Minimums.
	 * 			3. Evtl. senden von Events über den MiniDataMamager zum Gerät.
	 * 			4. Evtl. verschieben der Matrix nach Vorne in der Aufnahme, sodas dass letzte Minimum wieder an am Index RECORDINGSNIPPETLENGTH / 2 ist.
	 */
	private void next() {
		MidiGroup e = getNextEvents();
		totalStreamedGroups++;
		streamBuffer.addElement(e);
		//Distanz wird ermittelt.
		for(int i = 0; i < recordSnippet.size(); i++) {
			matrix[streamBuffer.getCurrentIndex()][i] = MidiGroup.getDist(e, recordSnippet.get(i)) + min(neighboursOf(streamBuffer.getCurrentIndex(),i));
		}
		int currentMinIndex = getMinIndex(streamBuffer.getCurrentIndex());

		//Sendet alle Events die aus solche Markiert sind die zwischen dem letzten Minimum und dem letzigen Minimum liegen an das Gerät.
		int i = lastMinIndex;
		int j = 0;
		if(lastMinIndex < currentMinIndex) {
			while ((i + j) % recordSnippet.size() != (currentMinIndex + 1) % recordSnippet.size() && j <= recordSnippet.size()) {
				if (befehleSparseArray.get(recordSnippet.offset + i + j) != null) {
					for (MidiEvent event : befehleSparseArray.get(recordSnippet.offset + i + j)) {
						MidiDataManager.getInstance().sendEvent(event);
					}
				} j++;
			} lastMinIndex = currentMinIndex;
		}

		//TODO: Hier gibts noch ein Problem: Wenn die Matrix nach unten (- deviation) verschoben wir? Was soll dann passieren bzw. wie kann ich das verhindern?
		//if(totalStreamedGroups >= STREAMBUFFERLENGTH / 2 && Math.abs(currentMinIndex - STREAMBUFFERLENGTH / 2) >= STREAMBUFFERLENGTH / 4) {
		if(totalStreamedGroups >= STREAMBUFFERLENGTH / 2 && currentMinIndex - STREAMBUFFERLENGTH / 2 >= STREAMBUFFERLENGTH / 4) {
			deviateMatrix(currentMinIndex - STREAMBUFFERLENGTH / 2);
			lastMinIndex -= currentMinIndex - STREAMBUFFERLENGTH / 2;
			currentMinIndex -= currentMinIndex - STREAMBUFFERLENGTH / 2;
		}

		//Das Ende der Aufnahme wurde erreicht. Abbruch des Algorithmus.
		//TODO: Es wird erst abgebrochen wenn das Minimum mut dem letzten Element aus der Aufnahme assoziiert wird. Dies ist aber kein Kriterium dass zwangsweise eintreten muss.
		if(currentMinIndex >= recordSnippet.offset + currentMinIndex) {
			this.stop();
		}
	}

	/**
	 * Ermittelt alle Nachbarn des Objektes an Position P(x,y). Die "Nachbarn" eines Objektes ist das Objekt links, oberhalb und link-oberhalb des Objektes.
	 * @param x : Die X-Index in der Matrix
	 * @param y : Der Y-Index in der Matrix.
	 * @return : alle nachbarn des Objektes
	 */
	private ArrayList<Float> neighboursOf(int x, int y) {
		ArrayList<Float> neighbours = new ArrayList<>(4);
		
		if(x > 0 && x != streamBuffer.getCurrentIndex() + 1) {
			neighbours.add(matrix[x - 1][y]);
			if(y > 0) {
				neighbours.add(matrix[x - 1][y - 1]);
			}
		} else if(x == 0 && totalStreamedGroups > STREAMBUFFERLENGTH) {
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

	/**
	 * Ermittelt den Minimalen Index in der Spalte col. Gibt es mehrere Elemente mit dem selben Wert, wird das letzte genommen.
	 * @param col : Die Spalte in der das Minimum ermittelt werden soll.
	 * @return minIndex: Den Index (Y-Koordinate in Matrix) in der sich das Minimum befindet. es gilt: matrix[minIndex][col] ist das Minimum aller Elemente aus matrix[a][col]
	 */
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

	/**
	 * Ermittelt das Minimum der Elemente aus der ArrayList List.
	 * @param list : Die Liste die auf sein Minimum untersucht werden soll.
	 * @return min: den niedrigsten Wert in der Liste list.
	 */
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

	/**
	 * Unused
	 */
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

	/**
	 * Verschiebt die Matrix um die deviation nach oben.
	 * @param deviation : Eine positive Zahl um die die Matrix verschoben werden soll.
	 */
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
				newMatrix[i][n] = MidiGroup.getDist(streamBuffer.get(i), recordSnippet.get(n)) + min(neighboursOf(i,n));
			}
		}
		this.matrix = newMatrix;
		Log.d("DTW","\n");
		Log.d("DTW",this.toString());
		Log.d("DTW","-----------------------------------------------------------------");
	}

	@Override
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
