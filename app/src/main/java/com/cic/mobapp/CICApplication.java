package com.cic.mobapp;

import android.app.Application;
import com.cic.mobapp.data.local.AppDatabase;
import com.cic.mobapp.data.local.SeedData;
import com.cic.mobapp.data.remote.RetrofitClient;
import java.util.concurrent.Executors;

public class CICApplication extends Application {

    private static CICApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        RetrofitClient.init(this);
        seedIfEmpty();
    }

    public static CICApplication getInstance() {
        return instance;
    }

    private void seedIfEmpty() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            if (db.eventDao().countById("seed_001") == 0)
                db.eventDao().upsertAll(SeedData.events());
            if (db.userDao().countById("mem_036") == 0)
                db.userDao().upsertAll(SeedData.users());
            if (db.resourceDao().countById("res_001") == 0)
                db.resourceDao().upsertAll(SeedData.resources());
            if (db.announcementDao().countById("ann_001") == 0)
                db.announcementDao().upsertAll(SeedData.announcements());
        });
    }
}
