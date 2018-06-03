package de.ostfalia.mobile.orgelhelfer.db;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.migration.Migration;

public class App extends Application {

    public static App INSTANCE;
    private static final String DATABASE_NAME = "Orgellager";

    private MyDatabase database;

    public static App get() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();




        // create database
        database = Room.databaseBuilder(getApplicationContext(), MyDatabase.class, DATABASE_NAME)
                .addMigrations(MyDatabase.MIGRATION_1_2)
                .addMigrations(MyDatabase.MIGRATION_2_3)
                .build();

        INSTANCE = this;
    }

    public MyDatabase getDB() {
        return database;
    }
}

