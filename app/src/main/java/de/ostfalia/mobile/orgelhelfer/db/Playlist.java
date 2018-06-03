package de.ostfalia.mobile.orgelhelfer.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Entity;

import java.util.ArrayList;
import java.util.List;

import de.ostfalia.mobile.orgelhelfer.model.MidiNote;
import de.ostfalia.mobile.orgelhelfer.model.MidiRecording;

@Entity
public class Playlist {


    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "name")
    private String name;


    public Playlist(String name) {
        this.name = name;

    }


    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
