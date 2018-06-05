package de.ostfalia.mobile.orgelhelfer.db;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;

@Database(entities = {Kategorie.class, Playlist.class, Track.class}, version = 1, exportSchema = false)
public abstract class MyDatabase extends RoomDatabase {
    public abstract KategorieDao kategorieDao();
    public abstract PlaylistDao playlistDao();
    public abstract TrackDao trackDao();

    static final Migration MIGRATION_3_1 = new Migration(3, 1) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {

        }
    };



}

