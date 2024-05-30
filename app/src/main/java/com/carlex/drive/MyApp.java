package com.carlex.drive;

import android.app.Application;
import androidx.room.Room;

public class MyApp extends Application {
    private static AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "my-database").build();
    }

    public static AppDatabase getDatabase() {
        return database;
    }
}

