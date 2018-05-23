package de.ostfalia.mobile.orgelhelfer.db;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Kategorie.class}, version = 1, exportSchema = false)
public abstract class MyDatabase extends RoomDatabase {
    public abstract KategorieDao kategorieDao();
}

