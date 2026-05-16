package com.cic.mobapp.util;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Singleton that broadcasts session lifecycle events app-wide.
 * MainActivity observes getExpired() and redirects to LoginActivity on true.
 */
public class SessionManager {

    private static volatile SessionManager instance;

    private final MutableLiveData<Boolean> expired = new MutableLiveData<>(false);

    private SessionManager() {}

    public static SessionManager get() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) instance = new SessionManager();
            }
        }
        return instance;
    }

    public LiveData<Boolean> getExpired() { return expired; }

    /** Call when a 401 is received on a non-auth endpoint. */
    public void onTokenInvalid() { expired.postValue(true); }

    /** Call after navigating away from MainActivity to reset state. */
    public void reset() { expired.postValue(false); }
}
