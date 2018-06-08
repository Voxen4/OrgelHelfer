package de.ostfalia.mobile.orgelhelfer.dtw;

import java.util.ArrayList;
import java.util.Collections;


public class Recording<E extends DtwComparable<E>> {
	private final int maxBufferLength;
	public int offset;
	private ArrayList<E> recordingList;
	private ArrayList<E> currentSnippet;
	
	Recording(ArrayList<E> recordingList, final int maxBufferLength) {
		this.recordingList = recordingList;
		Collections.sort(recordingList);
		offset = 0;
		this.maxBufferLength = maxBufferLength;
		currentSnippet = new ArrayList<>(maxBufferLength);
		for(int i = 0; i < recordingList.size() && i < maxBufferLength; i++) {
			currentSnippet.add(recordingList.get(i));
		}
	}

	/**
	 * Gibt das Element an der Stelle Index des jetzigen Ausschnittes zurück.
	 * @param index : der Index des Elements
	 * @return das Element an der Stelle index
	 */
	public E get(int index) {
		return currentSnippet.get(index);
	}
	
	public int getMaxBufferLength() {
		return maxBufferLength;
	}

	/**
	 * Gibt die länge des jetzigen Ausschnittes zurück. Ist dauerhaft <= maxBufferLength
	 * @return die länge des jezigen Ausschnittes
	 */
	public int getCurrentBufferSize() {
		return currentSnippet.size();
	}

	/**
	 * Bewegt die jetzigen Auschnitt der Aufnahme um amount.
	 * @param amount Die Richtung und Distanz der Bewegung.
	 */
	public void moveCurrentSnippet(int amount) {
		offset += amount;
		if(offset < 0 || offset >= maxBufferLength) {
			//TODO: Exception Werfen
		}
		currentSnippet.clear();
		for(int i = 0; i + offset < recordingList.size() && i < maxBufferLength; i++) {
			currentSnippet.add(recordingList.get(i + offset));
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(E e: currentSnippet) {
			sb.append(e.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	private int getBestEventIndex(E event, int index) {
		int ug = index + offset, og = index + offset;
		if(ug < recordingList.size()) {
			E currentMinEvent = this.recordingList.get(index + offset);
			int i = 1;
			//Bestimmen der Unteren grenze
			while(index + offset - i >= 0 && Math.abs(currentMinEvent.getTimestamp() - recordingList.get(index + offset - i).getTimestamp()) < Dtw.DELTAREACTIONTIME) {
				ug = index + offset - i;
				i++;
			}
			i = 1;
			//Bestimmen oder Oberen Grenze
			while(index + offset + i < recordingList.size() && Math.abs(currentMinEvent.getTimestamp() - recordingList.get(index + offset + i).getTimestamp()) < Dtw.DELTAREACTIONTIME) {
				og = index + offset + i;
				i++;
			}
			boolean foundNew = false;
			long deltaMinValaue = Dtw.DELTAREACTIONTIME;
			int deltaMinIndex = ug;
			for(int n = ug; n <= og; n++) {
				if(recordingList.get(n).dtwCompareTo(event) == 0 && n != index + offset) {
					if(recordingList.get(n).getTimestamp() - recordingList.get(deltaMinIndex).getTimestamp() < deltaMinValaue) {
						deltaMinValaue = recordingList.get(n).getTimestamp() - recordingList.get(deltaMinIndex).getTimestamp();
						deltaMinIndex = n;
					}
					foundNew = true;
				}
			}
			if(foundNew) {
				return deltaMinIndex;
			}
		}
		return index + offset;
	}

	/**
	 * Prüt ob innerhalb eines festen Zeitfensters (Dtw.DELTAREACTIONTIME) um das Element rec.get(index) ein anderes MidiEvent existiert für das gilt:
	 * MidiEvent e = rec.get(index).dtwComparable(event) == 0 (der Typ und der Pitch der MidiEvens ist gleich)
	 * Wenn ein besseres Event wird die methode swap() aufgerufen.
	 * @param event : Das Event dass gespielt wurde.
	 * @param index : Der Index des Elements mit der zurzeit besten Assoziation mit event
	 * @return true, wenn ein besseres Element gefunden und die Elemente vertauscht wurden, sost false
	 */
	public boolean lookForBetterEvents(E event, int index) {
		int j = getBestEventIndex(event, index);
		int i = offset + index;
		if(i != j) {
			System.out.println(index + " index in question");
			swap(i,j);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Vertauscht die Position der Elemente an den Stellen index1 und index2 sowohl in recordingList als auch in currentSnippet.
	 * @param index1 : Index des ersten ELements.
	 * @param index2 : Index des zweiten Elements.
	 */
	private void swap(int index1, int index2) {
		int lIndex,hIndex;
		if(index1 < index2) {
			lIndex = index1;
			hIndex = index2;
		} else if(index1 > index2){
			lIndex = index2;
			hIndex = index1;
		} else {
			System.out.println("SWAPING DENIED");
			return;
		}
		E lElement = recordingList.get(lIndex);
		E hElement = recordingList.get(hIndex);
		recordingList.remove(hIndex);
		recordingList.remove(lIndex);
		recordingList.add(lIndex, hElement);
		recordingList.add(hIndex, lElement);
		if(hIndex < getCurrentBufferSize()) {
			currentSnippet.remove(hIndex - offset);
			currentSnippet.remove(lIndex - offset);
			currentSnippet.add(lIndex - offset, hElement);
			currentSnippet.add(hIndex - offset, lElement);
		}
		System.out.println("Swapped row " + (hIndex - offset) + " with " + (lIndex - offset));
	}
}
