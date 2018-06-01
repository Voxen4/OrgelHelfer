package de.ostfalia.mobile.orgelhelfer.dtw;

public interface DtwComparable<T> extends Comparable<T> {
	public int dtwCompareTo(T o);
	
	public int compareTo(T o);
	
	public long getTimestamp();

	public boolean listenTo();
}
