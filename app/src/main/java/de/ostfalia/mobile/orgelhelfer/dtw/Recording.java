package de.ostfalia.mobile.orgelhelfer.dtw;

import java.util.ArrayList;
import java.util.Collections;

public class Recording<E extends DtwComparable<E>> {
	private final int maxBufferLength;
	public int offset;
	int currentIndex;
	public ArrayList<E> recordingList;
	public ArrayList<E> currentSnippet;
	
	public Recording(ArrayList<E> recordingList, final int maxBufferLength) {
		this.recordingList = recordingList;
		Collections.sort(recordingList);
		offset = 0;
		this.maxBufferLength = maxBufferLength;
		currentSnippet = new ArrayList<>(maxBufferLength);
		for(int i = 0; i < recordingList.size() && i < maxBufferLength; i++) {
			currentSnippet.add(recordingList.get(i));
		}
	}
	
	public E get(int index) {
		return currentSnippet.get(index);
	}
	
	public int getMaxBufferLength() {
		return maxBufferLength;
	}
	
	public int getCurrentBufferSize() {
		return currentSnippet.size();
	}
	
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
	
	public int getBestEventIndex(E event, int index) {
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
	
	private void swap(int index1, int index2) {
		int lIndex = 0, hIndex = 0;
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
		/*recordingList.remove(hIndex + offset);
		recordingList.remove(lIndex + offset);
		recordingList.add(lIndex + offset, hElement);
		recordingList.add(hIndex + offset, lElement);
		if(hIndex < getCurrentBufferSize()) {
			currentSnippet.remove(hIndex);
			currentSnippet.remove(lIndex);
			currentSnippet.add(lIndex, hElement);
			currentSnippet.add(hIndex, lElement);
		}*/
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
