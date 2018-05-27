package de.ostfalia.mobile.orgelhelfer.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PlaylistDao {

    @Query("SELECT * FROM playlist")
    List<Playlist> getAll();

    @Insert
    void insertAll(List<Playlist> playlists);

    @Insert
    void insertOne(Playlist playlist);

    @Delete
    void delete(Playlist playlist);

}
