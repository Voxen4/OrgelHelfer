package de.ostfalia.mobile.orgelhelfer.dtw;

/**
 * Interface das eine Klasse implementieren muss der Dtw-Algorithmus funktioniert.
 * @param <T>
 */
public interface DtwComparable<T> extends Comparable<T> {
    /**
     * Diese Methode bestimmt den Abstand bzw. die änhlichkeit von zwei Objekten.
     * Wie genau der Abstand errechnet wird, ist dem Benutzer überlassen.
     * @param o : Das Objekt zu dem die Distanz errechnet werden.
     * @return ein Wert der die Ähnlichkeit der beiden Objekte bezeichnet.
     */
    int dtwCompareTo(T o);

    /**
     * Diese Methode sollte die natürliche Ordnung (nach Timestamp) gewährleisten.
     * @param o : Das Objekt dass mit diesem verglichen werden soll.
     * @return -1 falls o < this ist, 0 falls this == o oder 1 falls o > this
     */
    int compareTo(T o);

    /**
     * Git den Timestamp des Objektes zurück.
     * @return den Timestamp des dieses Objektes.
     */
    long getTimestamp();
}
