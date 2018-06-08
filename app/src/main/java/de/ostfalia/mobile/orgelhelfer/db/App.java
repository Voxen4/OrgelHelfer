package de.ostfalia.mobile.orgelhelfer.db;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.migration.Migration;


/**
 * Singelton Class for the database
 */
public class App extends Application {

    public static App INSTANCE;
    private static final String DATABASE_NAME = "Orgellager";

    private MyDatabase database;

    // Return intance of the database which was created before in method "oncreate"
    public static App get() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();




        // create database
        database = Room.databaseBuilder(getApplicationContext(), MyDatabase.class, DATABASE_NAME)
                .addMigrations(MyDatabase.MIGRATION_3_1)
                .build();

        INSTANCE = this;
    }

    public MyDatabase getDB() {
        return database;
    }
}

