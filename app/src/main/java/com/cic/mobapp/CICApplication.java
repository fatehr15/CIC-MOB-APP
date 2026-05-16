package com.cic.mobapp;

import android.app.Application;
import com.cic.mobapp.data.remote.RetrofitClient;

public class CICApplication extends Application {

    private static CICApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        RetrofitClient.init(this);
    }

    public static CICApplication getInstance() {
        return instance;
    }
}
