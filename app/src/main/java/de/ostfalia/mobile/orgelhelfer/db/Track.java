package de.ostfalia.mobile.orgelhelfer.db;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

@Entity
public class Track {

    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo
    private String trackTitel;

    @ColumnInfo
    private String playlistName;

    @ColumnInfo
    private String jsonObjectString;

    public Track(String trackTitel, String playlistName, String jsonObjectString) {
        this.trackTitel = trackTitel;

        this.playlistName = playlistName;
        this.jsonObjectString=jsonObjectString;
    }


    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName (String name) {
        this.playlistName=name;
    }

    public void setJsonObjectString(String json) {
        this.jsonObjectString=json;
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

    public String getJsonObjectString () {
        return jsonObjectString;
    }

}
