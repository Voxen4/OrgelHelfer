package de.ostfalia.mobile.orgelhelfer.db;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;

@Database(entities = {Kategorie.class, Playlist.class, Track.class}, version = 3, exportSchema = false)
public abstract class MyDatabase extends RoomDatabase {
    public abstract KategorieDao kategorieDao();
    public abstract PlaylistDao playlistDao();
    public abstract TrackDao trackDao();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {

        }
    };
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {

        }
    };
}

