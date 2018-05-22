package de.ostfalia.mobile.orgelhelfer.db;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface KategorieDao {

    @Query("SELECT * FROM kategorie")
    List<Kategorie> getAll();

    @Insert
    void insertAll(List<Kategorie> kategorien);

    @Insert
    void insertOne(Kategorie kategorie);

    @Delete
    void delete(Kategorie Kategorie);

}

