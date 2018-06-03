package de.ostfalia.mobile.orgelhelfer.dtw;

/**
 * StreamBuffer-Klasse //TODO: besserer Name. Ist n�mlich kein Buffer
 * Diese Klasse ist eine eigene Datenstruktur die die letzten bufferLength Elemente speichert.
 * Wurden insgesamt mehr als bufferLength Elemente hinzugef�gt, so werden die ersten Elemente aus dem Array der Reihe nach �berschrieben.
 * Das zurzeit zuletzt hinzugef�gte Element befindet sich an der position von currentElement.
 * @author Aaron
 *
 */
public class StreamBuffer<E> {
    public E[] buffer;
    private int currentElement = -1;
    private int size;
	
	@SuppressWarnings("unchecked")
	StreamBuffer(final int bufferLength) {
		size = 0;
		buffer = (E[]) new Object[bufferLength];
	}
	
	public void addElement(E element) {
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
	
	public int getLastIndex() {
		if(size > buffer.length) {
			return (currentElement + 1) % buffer.length;
		}
		return 0;
	}
	
	public E get(int index) {
		return buffer[index];
	}
	
	public int getCurrentBufferSize() {
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
