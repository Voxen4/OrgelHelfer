package de.ostfalia.mobile.orgelhelfer.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Entity;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import de.ostfalia.mobile.orgelhelfer.model.MidiNote;
import de.ostfalia.mobile.orgelhelfer.model.MidiRecording;

@Entity
public class Playlist{


    @PrimaryKey(autoGenerate = true)
    private int uuid;

    @ColumnInfo(name = "name")
    private String name;

    public Playlist (String name) {
        this.name = name;
    }

    public int getUuid() {
        return uuid;
    }

    public void setUuid(int uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}