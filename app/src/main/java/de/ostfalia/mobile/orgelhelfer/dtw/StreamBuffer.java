package de.ostfalia.mobile.orgelhelfer.dtw;

/**
 * Die StreamBuffer Klasse speichert die letzten bufferLength Elemente in einem Array.
 * Sollte das Array voll sein wird das erste zugefügt Element überschrieben.
 * @author Aaron
 *
 */
public class StreamBuffer<E> {
	private E[] buffer;
	// Zeigt dauerhaft das auf zuletzt hinzugefügte Element.
    private int currentIndex = -1;
    // Ist immer <= bufferLength.
    private int size;

	/**
	 * Erzeugt ein neues StreamBufferObjekt mit einer festen länge bufferLength-
	 * @param bufferLength : Die Maximallänge des Buffers.
	 */
	@SuppressWarnings("unchecked")
	StreamBuffer(final int bufferLength) {
		size = 0;
		buffer = (E[]) new Object[bufferLength];
	}

	/**
	 * Fügt ein Element dem Buffer hinzu.
	 * @param element : Das Element dass hinzugefügt werden soll.
	 */
	public void addElement(E element) {
		size++;
		if(size > buffer.length) {
			size = buffer.length;
		}
		currentIndex = (currentIndex + 1) % buffer.length;
		buffer[currentIndex] = element;
	}

	/**
	 * Gibt den Index des zuletzt hinzugefügten Elements zurück.
	 * @return
	 */
	public int getCurrentIndex() {
		return currentIndex;
	}

	/**
	 * Gibt den Index des zuert hinzugefügten Elements zurück.
	 * @return
	 */
	public int getLastIndex() {
		if(size > buffer.length) {
			return (currentIndex + 1) % buffer.length;
		}
		return 0;
	}

	/**
	 * Gibt das Element an dem übergebenen index zurück.
	 * @param index : Index es zurückgegebenen Elements
	 * @return ein Element an der Stelle index.
	 */
	public E get(int index) {
		return buffer[index];
	}

	/**
	 * Gibt die Anzahl der gespeicherten Element im Buffer zurück. Die zurückgegebene Zahl ist dauerhaft <= bufferLength.
	 * @return : Anzahl der im Buffer gespeicherten Elemente.
	 */
	public int getCurrentBufferSize() {
		return size;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < buffer.length && buffer[i] != null; i++) {
			sb.append(buffer[i].toString());
			sb.append((char) 9);
			if(i == currentIndex) {
				sb.append("|");
				sb.append((char) 9);
			}
		}
		return sb.toString();
	}
}
