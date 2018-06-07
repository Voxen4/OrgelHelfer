package de.ostfalia.mobile.orgelhelfer.db;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Kategorie {



    @PrimaryKey(autoGenerate = true)
    private int kategorieID;


    @ColumnInfo(name = "name")
    private String name;


    public Kategorie(String name){
        this.name = name;

    }

    public int getKategorieID() {
        return kategorieID;
    }

    public void setKategorieID(int kategorieID) {
        this.kategorieID = kategorieID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }


}
