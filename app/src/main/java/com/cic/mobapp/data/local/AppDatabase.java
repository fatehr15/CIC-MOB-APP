package com.cic.mobapp.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.cic.mobapp.data.local.dao.EventDao;
import com.cic.mobapp.data.local.dao.ResourceDao;
import com.cic.mobapp.data.local.dao.UserDao;
import com.cic.mobapp.data.local.entity.EventEntity;
import com.cic.mobapp.data.local.entity.ResourceEntity;
import com.cic.mobapp.data.local.entity.UserEntity;

@Database(
        entities = {UserEntity.class, EventEntity.class, ResourceEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract EventDao eventDao();
    public abstract ResourceDao resourceDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "cic_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
