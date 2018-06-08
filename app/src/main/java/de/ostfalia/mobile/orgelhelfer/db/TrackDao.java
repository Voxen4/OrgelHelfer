package de.ostfalia.mobile.orgelhelfer.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface TrackDao {


    @Query("SELECT * FROM Track")
    List<Track> getAll();

    @Query("SELECT * FROM Track WHERE playlistName LIKE :playlist")
    List<Track> getAllTracks(String playlist);

    @Query("SELECT * FROM Track")
    List<Track> getTracks();

    @Query("SELECT * FROM Track WHERE kategorieFremdschluessel Like :kategorieFremdschluessel")
    List<Track> loadAllKategorieTracks(int kategorieFremdschluessel);

    @Query("SELECT * FROM Track WHERE playlistFremdschluessel = :playlistFremdschluessel")
    List<Track> loadAllPlaylistTracks(int playlistFremdschluessel);

    @Insert
    void insertAll(List<Track> trackList);

    @Insert
    void insertOne(Track track);

    @Update
    void updateOne(Track track);

    @Delete
    void delete(Track track);


}
