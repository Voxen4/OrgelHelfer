package de.ostfalia.mobile.orgelhelfer.db;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Kategorie {

    @PrimaryKey(autoGenerate = true)
    private int uid;


    @ColumnInfo(name = "name")
    private String name;


    public Kategorie(String name){
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
