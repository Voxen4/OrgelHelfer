package de.ostfalia.mobile.orgelhelfer.db;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Track {

    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo
    private String trackTitel;

    @ColumnInfo
    private String kuenstlerName;

    @ColumnInfo
    private double lengthTrack;

    public Track(String trackTitel, String kuenstlerName, double lengthTrack) {
        this.trackTitel = trackTitel;
        this.kuenstlerName = kuenstlerName;
        this.lengthTrack = lengthTrack;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getTrackTitel() {
        return trackTitel;
    }

    public void setTrackTitel(String trackTitel) {
        this.trackTitel = trackTitel;
    }

    public String getKuenstlerName() {
        return kuenstlerName;
    }

    public void setKuenstlerName(String kuenstlerName) {
        this.kuenstlerName = kuenstlerName;
    }

    public double getLengthTrack() {
        return lengthTrack;
    }

    public void setLengthTrack(double lengthTrack) {
        this.lengthTrack = lengthTrack;
    }
}
