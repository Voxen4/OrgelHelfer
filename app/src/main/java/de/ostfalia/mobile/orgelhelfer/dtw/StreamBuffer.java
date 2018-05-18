package de.ostfalia.mobile.orgelhelfer.dtw;

/**
 * StreamBuffer-Klasse //TODO: besserer Name. Ist n�mlich kein Buffer
 * Diese Klasse ist eine eigene Datenstruktur die die letzten bufferLength Elemente speichert.
 * Wurden insgesamt mehr als bufferLength Elemente hinzugef�gt, so werden die ersten Elemente aus dem Array der Reihe nach �berschrieben.
 * Das zurzeit zuletzt hinzugef�gte Element befindet sich an der position von currentElement.
 * @author Aaron
 *
 */
public class StreamBuffer {
	int currentElement = -1;
	int size;
	MidiList[] buffer;
	public StreamBuffer(int bufferLength) {
		size = 0;
		buffer = new MidiList[bufferLength];
	}
	
	public void addElement(MidiList element) {
		size++;
		if(size > buffer.length) {
			size = buffer.length;
		}
		currentElement = (currentElement + 1) % buffer.length;
		buffer[currentElement] = element;
	}
	
	public int getCurrentIndex() {
		return currentElement;
	}
	
	public MidiList get(int index) {
		return buffer[index];
	}
	
	public int size() {
		return size;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < buffer.length && buffer[i] != null; i++) {
			sb.append(buffer[i].toString());
			sb.append((char) 9);
			if(i == currentElement) {
				sb.append("|");
				sb.append((char) 9);
			}
		}
		return sb.toString();
	}
}
