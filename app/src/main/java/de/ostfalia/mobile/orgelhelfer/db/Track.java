package de.ostfalia.mobile.orgelhelfer.db;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

@Entity(foreignKeys = {@ForeignKey(
        entity = Kategorie.class,
        childColumns = "kategorieFremdschluessel",
        parentColumns = "kategorieID",
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
),
        @ForeignKey(
                entity = Playlist.class,
                childColumns = "playlistFremdschluessel",
                parentColumns = "uuid",
                onUpdate = ForeignKey.CASCADE,
                onDelete = ForeignKey.CASCADE
        )
})

public class Track {

    @PrimaryKey(autoGenerate = true)
    private int uid;

    private String trackTitel;
    private String playlistName;
    private String jsonObjectString;



    private Integer kategorieFremdschluessel;
    private Integer playlistFremdschluessel;

    @Ignore
    public Track(String trackTitel, String playlistName, String jsonObjectString) {
        this.trackTitel = trackTitel;
        this.playlistName = playlistName;
        this.jsonObjectString = jsonObjectString;
    }

    public Track(String trackTitel, String jsonObjectString) {
        this.trackTitel = trackTitel;
        this.jsonObjectString = jsonObjectString;
    }

    public Integer getKategorieFremdschluessel() {
        return kategorieFremdschluessel;
    }

    public void setKategorieFremdschluessel(Integer kategorieFremdschluessel) {
        this.kategorieFremdschluessel = kategorieFremdschluessel;
    }

    public Integer getPlaylistFremdschluessel() {
        return playlistFremdschluessel;
    }

    public void setPlaylistFremdschluessel(Integer playlistFremdschluessel) {
        this.playlistFremdschluessel = playlistFremdschluessel;
    }



    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String name) {
        this.playlistName = name;
    }

    public void setJsonObjectString(String json) {
        this.jsonObjectString = json;
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

    public JSONObject getJsonObject() throws JSONException {

        return new JSONObject(jsonObjectString);
    }

    public String getJsonObjectString() {
        return jsonObjectString;
    }

}