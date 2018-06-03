package de.ostfalia.mobile.orgelhelfer.dtw;

public interface DtwComparable<T> extends Comparable<T> {
    int dtwCompareTo(T o);

    int compareTo(T o);

    long getTimestamp();
}
